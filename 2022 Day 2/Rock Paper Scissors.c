#include <stdio.h>
#include <stdlib.h>

enum shape {
	rock, paper, scissor
};

char *shape_str[] = {"rock", "paper", "scissor"};
char *strategy_str[] = {"lose", "draw", "win"};

enum strategy {
	lose, draw, win
};

typedef enum strategy strategy;

typedef enum shape shape;

// player_hand_matrix[opponent_hand][outcome]
shape player_hand_matrix[3][3] = {
	            // lose     draw     win
	{/* rock    */ scissor, rock,    paper  },
	{/* paper   */ rock,    paper,   scissor},
	{/* scissor */ paper,   scissor, rock   }
};

struct game_round {
	shape player_hand, opponent_hand;
	strategy outcome;
};

typedef struct game_round game_round;

shape parse_opponent_hand(char h)
{
	switch (h) {
		case 'A': return rock;
		case 'B': return paper;
		case 'C': return scissor;
		default: {
			fprintf(stderr, "parse_opponent_hand: unknown character: %c\n", h);
			exit(EXIT_FAILURE);
		}
	}
}

strategy parse_strategy(char h)
{
	switch(h) {
		case 'X': return lose;
		case 'Y': return draw;
		case 'Z': return win;
		default: {
			fprintf(stderr, "parse_strategy: unknown character: %c\n", h);
			exit(EXIT_FAILURE);
		}
	}
}

game_round *parse_next_game_round(FILE *strategy_guide_file)
{
	char opponent_hand_character, strategy_character;
	if (fscanf(strategy_guide_file, " %c %c", &opponent_hand_character, &strategy_character) < 2) return NULL;
	game_round *result = (game_round *) malloc(sizeof(game_round));
	result->opponent_hand = parse_opponent_hand(opponent_hand_character);
	result->outcome = parse_strategy(strategy_character);
	return result;
}

int shape_score(shape s)
{
	switch(s) {
		case rock: return 1;
		case paper: return 2;
		case scissor: return 3;
	}
}

int outcome_score(strategy outcome)
{
	switch(outcome - 1) {
		case -1: return 0;
		case 0: return 3;
		case 1: return 6;
		default: {
			fprintf(stderr, "outcome_score: invalid outcome %d\n", outcome);
			exit(EXIT_FAILURE);
		}
	}
}

int calculate_score(game_round *round)
{
	return outcome_score(round->outcome) + shape_score(round->player_hand);
}

shape calculate_hand_for_strategy(shape opponent_hand, strategy s)
{
	return player_hand_matrix[opponent_hand][s];
}

int main(int argc, char *argv[])
{
	FILE *strategy_guide_file = NULL;
	if (argc > 1) {
		strategy_guide_file = fopen(argv[1], "r");
	}
	if (strategy_guide_file == NULL) strategy_guide_file = stdin;

	int round_count = 0;
	int round_buffer_capacity = 1;
	game_round **rounds = (game_round **) malloc(sizeof(game_round *));
	game_round *parsed_round = NULL;
	while ((parsed_round = parse_next_game_round(strategy_guide_file)) != NULL) {
		if (round_buffer_capacity == round_count) {
			round_buffer_capacity *= 2;
			rounds = (game_round**) realloc(rounds, sizeof(game_round *) * round_buffer_capacity);
		}
		rounds[round_count] = parsed_round;
		round_count++;
	}

	int total_score = 0;
	for (int i = 0; i < round_count; i++) {
		rounds[i]->player_hand = calculate_hand_for_strategy(rounds[i]->opponent_hand, rounds[i]->outcome);
		fprintf(stderr, "%d: %d %d %d %d\n", i, rounds[i]->opponent_hand, rounds[i]->outcome, rounds[i]->player_hand, calculate_score(rounds[i]));
		total_score += calculate_score(rounds[i]);
	}
	fprintf(stdout, "total score = %d\n", total_score);

	round_buffer_capacity = 0;
	free(rounds);
}
