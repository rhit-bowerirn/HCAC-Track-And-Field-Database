create procedure DeleteAthlete(@firstName varchar(20), @lastName varchar(20), @year tinyint, @team varchar(20), @gender bit) as
begin
	--Validate parameters
	--Check for nulls
	if(@firstName is null) begin
		raiserror('First Name cannot be null', 14, 1);
		return 1
	end
	if(@lastName is null) begin
		raiserror('Last Name cannot be null', 14, 1);
		return 2
	end
	if(@year is null) begin
		raiserror('Year cannot be null', 14, 1);
		return 3
	end
	if(@team is null) begin
		raiserror('Team cannot be null', 14, 1);
		return 4
	end
	if(@gender is null) begin
		raiserror('Gender cannot be null', 14, 1);
		return 5
	end

	--Check that the athlete exists
	if(not exists (select * from Person p join Athlete a on p.ID = a.ID join Team t on p.TeamID = t.ID
			   where p.FirstName = @firstName and  p.LastName = @lastName and a.YearInSchool = @year 
			   and a.Gender = @gender and t.SchoolName = @team)) begin
		raiserror('There are no records for this athlete in the DataBase', 14, 1);
		return 6
	end

	--We need the athleteID for easy reference
	declare @athleteID int = (select p.ID from Person p join Athlete a on p.ID = a.ID join Team t on p.TeamID = t.ID
							  where p.FirstName = @firstName and  p.LastName = @lastName and a.YearInSchool = @year 
							  and a.Gender = @gender and t.SchoolName = @team)

	--Delete from Participates table
	delete Participates
	where AthleteID = @athleteID

	--Delete from Athlete table
	delete Athlete
	where ID = @athleteID

	--Delete from Person table
	delete Person
	where ID = @athleteID

	return 0
end