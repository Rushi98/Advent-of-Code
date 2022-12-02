#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <stdbool.h>

struct inventory {
	int item_count;
	int *calories;
	int total_calories;
};

typedef struct inventory inventory;

int item_buffer_size = 0;
int *item_buffer = NULL;
char string_buffer[1024];

bool has_digits(char *str) {
	for (; *str; str++) {
		if (isdigit(*str)) {
			return true;
		}
	}
	return false;
}

inventory *read_elfs_inventory(FILE *inventory_file)
{
	fprintf(stderr, "read_elfs_inventory\n");
	int item_count = 0;
	string_buffer[0] = 0;
	while ((fgets(string_buffer, 1024, inventory_file) != NULL) && !has_digits(string_buffer));
	if (!has_digits(string_buffer)) {
		fprintf(stderr, "read_elf_inventory EOF\n");
		return NULL;
	}
	do {
		fprintf(stderr, "%s", string_buffer);
		if (item_count == item_buffer_size) {
			item_buffer_size = item_buffer_size ? 2 * item_buffer_size : 1;
			item_buffer = (int *) realloc(item_buffer, item_buffer_size * sizeof(int));
		}
		item_buffer[item_count] = strtol(string_buffer, NULL, 10);
		item_count++;
		string_buffer[0] = 0;
	} while (fgets(string_buffer, 1024, inventory_file) && has_digits(string_buffer));

	inventory *result = (inventory *) malloc(sizeof(inventory));
	result->total_calories = 0;
	result->item_count = item_count;
	result->calories = (int *) calloc(sizeof(int), item_count);
	for (int i = 0; i < item_count; i++) {
		result->calories[i] = item_buffer[i];
		result->total_calories += item_buffer[i];
	}
	fprintf(stderr, "read_elf_inventory: total_calorie %d\n", result->total_calories);
	return result;
}

static
int compare(const void *a, const void *b)
{
	int *f = (int *) a;
	int *s = (int *) b;
	return (*s) - (*f);
}

int main(int argc, char *argv[])
{
	FILE *inventory_file = NULL;
	if (argc > 1) {
		inventory_file = fopen(argv[1], "r");
	}
	if (inventory_file == NULL) inventory_file = stdin;

	int elf_count = 0;
	inventory **inventories_buffer = NULL;
	int inventories_buffer_capacity = 0;
	inventory *current_inventory = NULL;
	while ((current_inventory = read_elfs_inventory(inventory_file)) != NULL) {
		if (elf_count == inventories_buffer_capacity) {
			inventories_buffer_capacity = inventories_buffer_capacity ? 2 * inventories_buffer_capacity : 1;
			inventories_buffer = (inventory **) realloc(inventories_buffer, sizeof(inventory *) * inventories_buffer_capacity);
		}
		inventories_buffer[elf_count] = current_inventory;
		elf_count++;
	}

	int top_3_total_calories[] = {0, 0, 0, 0};
	for (int i = 0; i < elf_count; i++) {
		top_3_total_calories[3] = inventories_buffer[i]->total_calories;
		qsort(top_3_total_calories, 4, sizeof(int), compare);
	}
	int sum = 0;
	for (int i = 0; i < 3; i++) {
		sum += top_3_total_calories[i];
		fprintf(stdout, "%d: %d\n", i + 1, top_3_total_calories[i]);
	}
	fprintf(stdout, "sum of top 3 = %d\n", sum);

	item_buffer_size = 0;
	free(item_buffer);
	inventories_buffer_capacity = 0;
	free(inventories_buffer);
}
