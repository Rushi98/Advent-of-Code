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

inventory *read_elfs_inventory(FILE *inventory_file)
{
	int item_count = 0;
	bool does_line_contain_digit = true;
	while (fgets(string_buffer, 1024, inventory_file) && does_line_contain_digit) {
		does_line_contain_digit = false;
		char *w = string_buffer;
		while (*w && !does_line_contain_digit) {
			does_line_contain_digit = isdigit(*w);
			w++;
		}
		if (does_line_contain_digit) {
			if (item_count == item_buffer_size) {
				item_buffer_size = item_buffer_size ? 2 * item_buffer_size : 1;
				item_buffer = (int *) realloc(item_buffer, item_buffer_size * sizeof(int));
			}
			item_buffer[item_count] = strtol(string_buffer, NULL, 10);
			item_count++;
		}
	}
	if (item_count == 0) return NULL;

	inventory *result = (inventory *) malloc(sizeof(inventory));
	result->total_calories = 0;
	result->item_count = item_count;
	result->calories = (int *) calloc(sizeof(int), item_count);
	for (int i = 0; i < item_count; i++) {
		result->calories[i] = item_buffer[i];
		result->total_calories += item_buffer[i];
	}
	return result;
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
			inventories_buffer = (inventory **) calloc(sizeof(inventory *), inventories_buffer_capacity);
		}
		inventories_buffer[elf_count] = current_inventory;
		elf_count++;
	}

	int max_total_calories = 0;
	for (int i = 0; i < elf_count; i++) {
		int current_total_calories = inventories_buffer[i]->total_calories;
		if (current_total_calories > max_total_calories) max_total_calories = current_total_calories;
	}
	fprintf(stdout, "%d\n", max_total_calories);

	item_buffer_size = 0;
	free(item_buffer);
	inventories_buffer_capacity = 0;
	free(inventories_buffer);
}
