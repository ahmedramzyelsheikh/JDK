// C++ version. Memory: RAII. std::vector and std::string free their memory automatically
// when they go out of scope (destructors), no garbage collector.
// Build: g++ -std=c++20 students.cpp -o students && ./students
#include <algorithm>
#include <iomanip>
#include <iostream>
#include <numeric>
#include <string>
#include <vector>

struct Student {
    std::string name;
    double grade;
};

int main() {
    std::vector<Student> students{   // template: a std::vector specialised for Student
        {"Ali", 88.5},
        {"Sara", 93.0},
        {"Omar", 76.0},
        {"Mona", 81.5},
    };

    std::ranges::sort(students, [](const Student& a, const Student& b) { return a.grade > b.grade; });

    double sum = std::accumulate(students.begin(), students.end(), 0.0,
                                 [](double acc, const Student& s) { return acc + s.grade; });

    std::cout << std::fixed << std::setprecision(2);
    for (const auto& s : students) {
        std::cout << std::left << std::setw(6) << s.name << " " << std::right << std::setw(6) << s.grade << "\n";
    }
    std::cout << "Average: " << sum / students.size() << "\n";
    return 0;
}   // students' destructor runs here and frees everything (RAII)
