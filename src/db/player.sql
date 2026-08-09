create table all_players (
    playerID int primary key,
    player_nickname varchar(48) not null,
    real_name varchar(48),
    exp_factor int,
    sets_played int,
    phone_number int
);


create table player_total_match_history (
    player_id int not null,

    primary key (player_id)

    foreign key (tournament_id)
        references global_tournaments(tournament_id)
        on delete cascade
    foreign key (match_d)
        references pool_matches(match_id)

);


create table player_points_map (
    player_id int not null,

    primary key (player_id, game_name)

    foreign key (tournament_id)
        references global_tournaments(tournament_id)
);

create table player_tier_map (
    player_id int not null,

    primary key (player_id)

    foreign key (game_name)

);