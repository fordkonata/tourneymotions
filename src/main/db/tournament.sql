create table tournaments (
    tournament_id bigserial primary key,
    tournament_name varchar(84) not null,
    total_entrants int
);

create table tournament_entrants (
    tournament_id bigint references tournaments(tournament_id) on delete cascade,
    player_id bigint references players(player_id),
    seed int,
    primary key (tournament_id, player_id)
);

create table brackets (
    bracket_id bigserial primary key,
    tournament_id bigint not null references tournaments(tournament_id) on delete cascade,
    game_name varchar(84) not null references games(game_name),
    bracket_tier int not null
);

create table bracket_entrants (
    bracket_id bigint references brackets(bracket_id) on delete cascade,
    player_id bigint references players(player_id),
    primary key (bracket_id, player_id)
);

create table pools (
    pool_id bigserial primary key,
    bracket_id bigint not null references brackets(bracket_id) on delete cascade,
    stage_number int not null,
    pool_position int not null,
    unique (bracket_id, stage_number, pool_position)
);

create table matches (
    match_id bigserial primary key,
    pool_id bigint not null references pools(pool_id) on delete cascade,

    match_position int,
    match_side varchar(24) not null,

    p1 bigint references players(player_id),
    p2 bigint references players(player_id),
    winner bigint references players(player_id),
    loser bigint references players(player_id),

    check (match_side in ('winners', 'losers', 'preliminaries')),

    unique (pool_id, match_side, match_position)
);