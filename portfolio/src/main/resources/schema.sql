
drop table if exists Sprint;
drop table if exists Project;
drop table if exists Planner;


CREATE TABLE Planner(
    id int auto_increment primary key,
    name varchar(60)
);

CREATE TABLE Project(
    id int auto_increment primary key,
    planner_id int not null references Planner(id),
    name varchar(60) not null,
    start_date date not null,
    end_date date not null,
    description varchar(3000) not null,
    time_deactivated DATETIME,
    constraint chk_date_1 CHECK(end_date >= start_date)
);

CREATE TABLE Sprint(
    id UUID primary key,
    project_id int not null references Project(id),
    name varchar(60) not null,
    start_date DATE not null,
    end_date DATE not null,
    description varchar(3000) not null,
    colour varchar(7) default '#000000',
    time_deleted DATETIME,
    constraint chk_date CHECK(end_date >= start_date)
);

CREATE TABLE Event(
    id         UUID primary key,
    project_id int         not null references Project (id),
    name       varchar(60) not null,
    start_date DATETIME        not null,
    end_date   DATETIME        not null,
    start_date_colour varchar(7),
    end_date_colour varchar(7)

);


