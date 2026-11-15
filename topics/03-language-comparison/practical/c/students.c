/*
 * C version. Memory: manual malloc/free, no classes, no generics.
 * Build: gcc students.c -o students && ./students
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
    char name[32];          /* fixed-size char array, must fit the name + '\0' */
    double grade;
} Student;

/* qsort needs a comparison function taking void pointers (no generics in C) */
static int by_grade_desc(const void *a, const void *b) {
    const Student *x = (const Student *)a;
    const Student *y = (const Student *)b;
    return (x->grade < y->grade) - (x->grade > y->grade);
}

int main(void) {
    size_t count = 4;
    Student *students = malloc(count * sizeof(Student));   /* heap allocation */
    if (students == NULL) {
        return 1;                                           /* error codes, no exceptions */
    }

    strcpy(students[0].name, "Ali");  students[0].grade = 88.5;
    strcpy(students[1].name, "Sara"); students[1].grade = 93.0;
    strcpy(students[2].name, "Omar"); students[2].grade = 76.0;
    strcpy(students[3].name, "Mona"); students[3].grade = 81.5;

    qsort(students, count, sizeof(Student), by_grade_desc);

    double sum = 0;
    for (size_t i = 0; i < count; i++) {
        printf("%-6s %6.2f\n", students[i].name, students[i].grade);
        sum += students[i].grade;
    }
    printf("Average: %.2f\n", sum / count);

    free(students);          /* forget this = memory leak; call twice = undefined behaviour */
    return 0;
}
