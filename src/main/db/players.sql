create table players (
    player_id bigserial primary key,
    player_nickname varchar(48) not null,
    real_name varchar(48),
    phone_number int
);


--might need to change this to include tournamentID or bracketID before production.
create table player_total_match_history (
    player_id bigint not null,
    match_id bigint not null,

    primary key (player_id, match_id),

    foreign key (player_id)
        references players(player_id),

    foreign key (match_id)
        references matches(match_id)
);

create table player_points_map (
    player_id bigint not null,
    game_name varchar(84),
    points int not null,

    primary key (player_id, game_name),

    foreign key (player_id)
        references players(player_id),

    foreign key (game_name)
        references games(game_name)
);

create table player_tier_map (
    player_id bigint not null,
    game_name varchar(84),
    tier int not null,

    primary key (player_id, game_name),

    foreign key (game_name)
        references games(game_name),

    foreign key (player_id)
        references players(player_id)
);

create table player_bracket_placements_map (
    player_id bigint not null,
    tournament_id bigint not null,
    game_name varchar(84),
    placement int,

    primary key (player_id, tournament_id, game_name),

    foreign key (player_id)
        references players(player_id),

    foreign key (game_name)
        references games(game_name),

    foreign key (tournament_id)
        references tournaments(tournament_id)
);

create table points_changed_history (
    player_id bigint not null,
    game_name varchar(84) not null,
    tournament_id bigint not null,

    primary key (player_id, game_name, tournament_id),

    foreign key (player_id)
        references players(player_id),

    foreign key (tournament_id)
        references tournaments(tournament_id),

    foreign key (game_name)
        references games(game_name)
);