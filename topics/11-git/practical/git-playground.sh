#!/usr/bin/env bash
# Builds a throw-away Git repository in a temp folder and walks through the scenarios of topic 11:
# commits, branches, merge, rebase, a conflict and its resolution, revert, reset, stash, tags,
# and recovering a "lost" commit with reflog. Nothing touches your real repositories.
# Run: bash topics/11-git/practical/git-playground.sh
set -euo pipefail

WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT
cd "$WORK"

step() { printf '\n\033[1;34m== %s ==\033[0m\n' "$1"; }
graph() { git log --oneline --graph --all --decorate | sed 's/^/   /'; }

git init -q -b main
git config user.name "Learner"
git config user.email "learner@example.com"
git config commit.gpgsign false
git config core.autocrlf false

step "1. First commits on main"
printf 'public class Price {\n    static double total(double p) { return p * 1.10; }\n}\n' > Price.java
git add Price.java && git commit -qm "feat: add price calculation"
echo "# Shop" > README.md
git add README.md && git commit -qm "docs: add readme"
graph

step "2. Feature branch + fast-forward merge"
git switch -qc feature/discount
printf 'class Discount { static double apply(double p) { return p * 0.9; } }\n' > Discount.java
git add Discount.java && git commit -qm "feat: add discount"
git switch -q main
git merge -q feature/discount                     # main had no new commits -> fast-forward
graph

step "3. Diverged branches + merge commit (--no-ff)"
git switch -qc feature/logging
echo "// logging added" >> Discount.java
git commit -qam "feat: log discounts"
git switch -q main
echo "Run: java Price.java" >> README.md
git commit -qam "docs: how to run"
git merge -q --no-ff feature/logging -m "Merge branch 'feature/logging'"
graph

step "4. Rebase a private branch onto main (linear history)"
git switch -qc feature/currency
echo "// currency support" >> README.md
git commit -qam "feat: currency support"
git switch -q main
printf 'class Tax { static final double VAT = 0.14; }\n' > Tax.java
git add Tax.java && git commit -qm "feat: add VAT constant"
git switch -q feature/currency
git rebase -q main                                 # replays our commit on top of main (new SHA)
git switch -q main && git merge -q feature/currency
graph

step "5. A conflict: two branches change the same line"
git switch -qc feature/vat
sed -i 's/return p \* 1.10;/return p * (1 + Tax.VAT);/' Price.java
git commit -qam "feat: use VAT constant"
git switch -q main
sed -i 's/return p \* 1.10;/return Math.round(p * 1.10 * 100) \/ 100.0;/' Price.java
git commit -qam "fix: round totals"
if ! git merge feature/vat -m "merge vat" > /dev/null 2>&1; then
  echo "   CONFLICT in Price.java:"
  sed 's/^/     | /' Price.java
  # resolve: keep both intentions (VAT constant + rounding), remove the markers
  printf 'public class Price {\n    static double total(double p) { return Math.round(p * (1 + Tax.VAT) * 100) / 100.0; }\n}\n' > Price.java
  git add Price.java
  git commit -qm "Merge branch 'feature/vat' (resolve conflict: VAT + rounding)"
  echo "   resolved:"
  sed 's/^/     | /' Price.java
fi
graph

step "6. Undo a pushed commit safely with revert"
echo "debug=true" > app.properties
git add app.properties && git commit -qm "chore: enable debug"
git revert --no-edit HEAD > /dev/null               # new commit that undoes the previous one
git log --oneline -3 | sed 's/^/   /'
echo "   app.properties exists? $(test -f app.properties && echo yes || echo no)"

step "7. reset --hard 'loses' a commit... reflog brings it back"
echo "important work" > notes.txt
git add notes.txt && git commit -qm "feat: important notes"
LOST=$(git rev-parse --short HEAD)
git reset -q --hard HEAD~1
echo "   after reset --hard: notes.txt exists? $(test -f notes.txt && echo yes || echo no)"
git reflog -3 | sed 's/^/   /'
git switch -qc rescue "$LOST"
echo "   rescued on branch 'rescue': notes.txt exists? $(test -f notes.txt && echo yes || echo no)"
git switch -q main

step "8. Stash unfinished work"
echo "half-done change" >> README.md
git stash push -qm "wip: readme"
echo "   working tree clean after stash: $(git status --porcelain | wc -l) changes"
git stash pop -q
echo "   after pop: $(git status --porcelain | wc -l) change(s) back"
git restore README.md

step "9. Tag a release (semantic versioning)"
git tag -a v1.0.0 -m "First release"
git describe --tags | sed 's/^/   /'

step "Final history"
graph
echo
echo "Done. The temporary repository is deleted on exit."
