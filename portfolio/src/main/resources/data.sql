Insert Into Planner(name) values ('Planner1');




insert into Project(planner_id, name, start_date, end_date, description) values (1, 'Project 2022', '2022-01-01', '2023-12-01','The spacecraft reproduces alignment like a futile processor.');
insert into Project(planner_id, name, start_date, end_date, description) values (1, 'Project Bravo', '2022-02-01', '2022-03-01','Try fluffing peanuts ricotta flavored with BBQ sauce.');
insert into Project(planner_id, name, start_date, end_date, description) values (1, 'Project Charlie', '2022-03-01', '2022-04-01','When the cannon laughs for puerto rico, all suns rob warm, wet gulls.');
insert into Project(planner_id, name, start_date, end_date, description) values (1, 'Project Delta', '2022-04-01', '2022-05-1','Who can view the moonlight and control of a guru if he has the fraternal paradox of the self?');
insert into Project(planner_id, name, start_date, end_date, description) values (1, 'Project Echo', '2020-05-01', '2030-01-01','A falsis, tumultumque magnum cotta.');




insert into Sprint(id,project_id, name, start_date, end_date,description, colour) values (random_uuid(), 2, 'Sprint 1','2022-01-01', '2022-02-01','Reincarnation, mineral and a beloved mind.', '#0057b7');
insert into Sprint(id, project_id, name, start_date, end_date,description, colour) values (random_uuid(), 2, 'Sprint 2','2022-02-01', '2022-03-01','Why does the emitter yell?','#ffd700');
insert into Sprint(id, project_id, name, start_date, end_date,description) values (random_uuid(), 2, 'Sprint 3','2022-03-01', '2022-04-01','Everyone loves the viscosity of ground beef frittata seasond with divided baking powder.');
insert into Sprint(id, project_id, name, start_date, end_date,description) values (random_uuid(), 2, 'Sprint 4','2022-04-01', '2022-05-01','Order of life will cheerfully synthesise a new lama.');
insert into Sprint(id, project_id, name, start_date, end_date,description) values (random_uuid(), 2, 'Sprint 5','2022-05-01', '2022-06-01','When grilling ground peanut butters, be sure they are room temperature.');
insert into Sprint(id, project_id, name, start_date, end_date,description) values (random_uuid(), 2, 'Sprint 6','2022-06-01', '2022-07-01','When grilling packaged pumpkin seeds, be sure they are room temperature.');
insert into Sprint(id, project_id, name, start_date, end_date,description) values (random_uuid(), 2, 'Sprint 7','2022-07-01', '2022-08-01','Assimilation at the radiation dome was the alarm of adventure, united to a carnivorous moon.');
insert into Sprint(id, project_id, name, start_date, end_date,description) values (random_uuid(), 2, 'Sprint 8','2022-08-01', '2022-09-01','All those advices will be lost in anomalies like attitudes in mysteries');






/**
  Below should cause issues.
 */

//insert into Sprint(project_id, name, start_date, end_date,description, is_active) values (3, 'Sprint 8','2023-08-01 20:00:00', '2022-09-01 23:00:00','All thy dates should make sense', 'y');