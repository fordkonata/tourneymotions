create table  players (
    playerID int primary key,
    player_nickname varchar(48) not null,
    real_name varchar(48),
    exp_factor int,
    sets_played int,
    phone_number int
);


create table player_total_match_history (
    player_id int not null,

    tournament_id int not null,
    bracket_id int not null,
    pool_id int not null,
    match_id int not null,

    primary key (player_id,tournament_id, bracket_id,pool_id, match_id),

    foreign key (player_id)
        references players(player_id),

    foreign key (tournament_id, bracket_id, pool_id, match_id)
        references tournament.matches(tournament_id, bracket_id, pool_id,match_id)
);


create table player_points_map (
    player_id int not null,

    primary key (player_id, game_name)

    foreign key (player_id)
        references players(player_id)

    foreign key (game_name)
        references game(game_id)

);

create table player_tier_map (
    player_id int not null,

    primary key (player_id, game_name)

    foreign key (game_name)

    foreign key (player_id)
        references players(player_id)
);

create table player_bracket_placements_map (
    player_id int not null,
    tournament_id int not null,

    primary key (player_id,
                tournament_id)

    foreign key (player_id)
        references players(player_id)

    foreign key (tournament_id)
        references tournament.tournaments(tournament_id)
);


create table points_changed_history (
    player_id int not null,
    game_name varchar(84) not null,

    primary key (player_id, game_name)

    foreign key (player_id)
        references players(player_id)

    foreign key (game_name)
        references games(game_name)
);