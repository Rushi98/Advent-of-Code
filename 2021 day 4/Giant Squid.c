#include <stdio.h>

typedef struct 
{
	int grid[5][5];
	int row_mark_count[5] = {0};
	int col_mark_count[5] = {0};
} board;

board* read_board() {
	board *result = (board *) malloc(sizeof(board));
	for (int i = 0; i < 5; i++) {
		for (int j = 0; j < 5; j++) {
			if (scanf("%d", &(result->grid[i][j])) == EOF) {
				free(result);
				return NULL;
			}
		}
	}
	return result;
}

bool mark(board *b, int draw) {
	for (int i = 0; i < 5; i++) {
		for (int j = 0; j < 5; j++) {
			if (b->grid[i][j] == draw) {
				b->row_mark_count[i]++;
				b->col_mark_count[j]++;
			}
		}
	}
	for (int i = 0; i < 5; i++) {
		if (b->row_mark_count[i] == 5 or b->col_mark_count[i] == 5) return true;
	}
	return false;
}


int main()
{
	int *draws;
	int total_draws = 0;
	board **boards;
	int total_boards = 0;

	char prev = ',';
	while (prev == ',') {
		total_draws++;
		draws = (int *) realloc(draws, total_draws * sizeof(int));
		scanf("%d", &draws[total_draws - 1]);
	}

	while (!feof(stdin)) {
		board *b = read_board();
		if (b == NULL) break;
		total_boards++;
		boards = (board**) realloc(boards, total_boards * (sizeof(board*)));
		boards[total_boards - 1] = b;
	}
}
