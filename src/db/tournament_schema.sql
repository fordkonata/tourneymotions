create schema tournament;

create table tournament.tournaments (
  tournament_id int not null primary key,
  tournament_name varchar(84) not null,
  total_entrants int
);


create table tournament.brackets (
    tournament_id int not null,
    bracket_id int not null,
    game_name varchar(84) not null,
    bracket_tier int not null,

    primary key (tournament_id, bracket_id),

    foreign key (tournament_id)
        references tournament.tournaments(tournament_id)
        on delete cascade
);


create table tournament.pools (
    tournament_id int not null,
    bracket_id int not null,
    pool_id int not null,

    primary key (tournament_id, bracket_id, pool_id),

    foreign key (tournament_id, bracket_id)
        references tournament.brackets(tournament_id, bracket_id)
        on delete cascade
);

create table tournament.matches (
    tournament_id int not null,
    bracket_id int not null,
    pool_id int not null,
    match_id int not null,

    match_position int,
    match_side varchar(24) not null,

    p1 int,
    p2 int,
    winner int,
    loser int,

    primary key ( tournament_id,bracket_id,pool_id,match_id),

    foreign key (tournament_id, bracket_id, pool_id )
     references tournament.pools(tournament_id, bracket_id, pool_id)
        on delete cascade,

    foreign key (p1)
        references players(player_id),

    foreign key (p2)
        references players(player_id),

    foreign key (winner)
        references players(player_id),

    foreign key (loser)
        references players(player_id),

    check (match_side in ('winners', 'losers', 'preliminaries')),

    unique (tournament_id, bracket_id, pool_id, match_side, match_position)
);

