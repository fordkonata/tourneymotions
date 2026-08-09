create table bracket (
    bracket_id int primary key not null,
    game_name varchar(56) not null,
    bracket_tier int not null,

    foreign key (tournament_id)
        references  tournament(tournament_id)
        ON DELETE CASCADE
);

