drop table if exists users, mpa, films, likes, genres, film_genres, friends, reviews;

create table if not exists users (
	id integer generated by default as identity not null primary key,
	login varchar(255) not null unique,
	name varchar(255) not null,
	email varchar(255) not null unique,
	birthday date
);

create table if not exists mpa (
	id integer generated by default as identity not null primary key,
	name varchar(255) not null unique
);

create table if not exists films (
	id integer generated by default as identity not null primary key,
	name varchar(255) not null,
	releaseDate date not null,
	description varchar(200),
	duration integer not null,
	mpa_id integer not null references mpa(id)
);

create table if not exists likes (
	user_id integer not null references users(id) on delete cascade,
	film_id integer not null references films(id) on delete cascade,
	primary key (user_id, film_id)
);

create table if not exists genres (
	id integer generated by default as identity not null primary key,
	name varchar(255) not null unique
);

create table if not exists film_genres (
	film_id integer not null references films(id) on delete cascade,
	genre_id integer not null references genres(id) on delete cascade,
	primary key (film_id, genre_id)
);

create table if not exists friends (
	user_id integer not null references users(id) on delete cascade,
	friend_id integer not null references users(id) on delete cascade,
	primary key (user_id, friend_id)
);

create table if not exists reviews (
    id integer generated by default as identity not null primary key,
    content varchar(255) not null,
    isPositive boolean not null,
    userId integer not null references users(id) on delete cascade,
    filmId integer not null references films(id) on delete cascade,
    useful integer
);