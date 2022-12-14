create proc FindAthletesMeets (
	@firstName varchar(20),
	@lastName varchar(20),
	@year tinyint,
	@gender bit,
	@teamName varchar(20)
)
AS
BEGIN

	--Validate Paramaters
	if(@firstName is null) BEGIN
		RAISERROR('Provided first name cannot be null.', 14, 1);
		return 1;
	END

	if(@lastName is null) BEGIN
		RAISERROR('Provided last name cannot be null.', 14, 1);
		return 2;
	END
	
	if(@year is null) BEGIN
		RAISERROR('Provided year in school cannot be null.', 14, 1);
		return 3;
	END	
	if(@teamName is null or not exists(select * from Team t where t.SchoolName = @teamName)) BEGIN
		RAISERROR('Provided team name cannot be null or must exist.', 14, 1);
		return 4;
	END
	if(@gender is null) begin
		raiserror('Gender cannot be null', 14, 1);
		return 5
	end
	
	declare @teamID int
	set @teamID = (select ID from Team t where t.SchoolName = @teamName);

	--Checks to see if athlete exists with the database
	if(not exists(select * from Athlete a join Person p on a.ID = p.ID 
	   where p.FirstName = @firstName and p.LastName = @lastName and a.YearInSchool = @year 
	   and a.Gender = @gender and p.TeamID = @teamID)) BEGIN
		RAISERROR('Provided athlete does not exist within the database', 14, 1);
		return 6;
    END

	declare @athleteID int
	set @athleteID = (select a.ID from Athlete a join Person p on a.ID = p.ID 
	   where p.FirstName = @firstName and p.LastName = @lastName and a.YearInSchool = @year 
	   and a.Gender = @gender and p.TeamID = @teamID)
	   

	-- Gets all of the event and meet info for the requested athlete
	select m.Name as 'Meet Name', m.Date as 'Meet Date', m.Location as 'Meet Location', e.Name as 'Event Name',
		p.Score as 'Score', p.Points as 'Points'
	from Participates p 
	join Meet m on m.ID = p.MeetID
	join [Event] e on e.ID = p.EventID
	where p.AthleteID = @athleteID

END