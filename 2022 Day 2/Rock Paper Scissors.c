#include <stdio.h>
#include <stdlib.h>

enum shape {
	rock, paper, scissor
};

enum strategy {
	lose, draw, win
};

typedef enum strategy strategy;

typedef enum shape shape;

struct game_round {
	shape player_hand, opponent_hand;
	strategy outcome;
};

typedef struct game_round game_round;

int calculate_outcome(shape player_hand, shape opponent_hand)
{
	int diff = player_hand - opponent_hand;
	if (diff < -1) diff += 3;
	if (diff > 1) diff -= 3;
	return diff - 1;
}

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

shape parse_player_hand(char h)
{
	switch(h) {
		case 'X': return rock;
		case 'Y': return paper;
		case 'Z': return scissor;
		default: {
			fprintf(stderr, "parse_player_hand: unknown character: %c\n", h);
			exit(EXIT_FAILURE);
		}
	}
}

game_round *parse_next_game_round(FILE *strategy_guide_file)
{
	char opponent_hand_character, player_hand_character;
	if (fscanf(strategy_guide_file, " %c %c", &opponent_hand_character, &player_hand_character) < 2) return NULL;
	game_round *result = (game_round *) malloc(sizeof(game_round));
	result->opponent_hand = parse_opponent_hand(opponent_hand_character);
	result->player_hand = parse_player_hand(player_hand_character);
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
	int outcome = calculate_outcome(round->player_hand, round->opponent_hand);
	return outcome_score(outcome) + shape_score(round->player_hand);
}

shape calculate_hand_for_strategy(shape opponent_hand, strategy s)
{
	int h = opponent_hand - s;
	if (h < -1) h += 2;
	if (h > 1) h -= 2;
	return h;
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
		total_score += calculate_score(rounds[i]);
	}
	fprintf(stdout, "total score = %d\n", total_score);

	round_buffer_capacity = 0;
	free(rounds);
}
