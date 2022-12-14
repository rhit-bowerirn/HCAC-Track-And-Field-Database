create or alter procedure AddAthlete(@firstName varchar(20), @lastName varchar(20), @year tinyint, @team varchar(20), @gender bit) as
begin
	--Validate parameters
	if(@firstName is null) begin
		raiserror('First Name cannot be null', 14, 1);
		return 1
	end
	if(@lastName is null) begin
		raiserror('Last Name cannot be null', 14, 1);
		return 2
	end
	if(@year is null or @year < 1) begin
		raiserror('Invalid year', 14, 1);
		return 3
	end
	if(@team is null) begin
		raiserror('Team cannot be null', 14, 1);
		return 4
	end
	if(@gender is null) begin
		raiserror('Gender cannot be null', 14, 1);
		return 5;
	end

	--Check for duplicates
	if(exists (select * from Person p join Athlete a on p.ID = a.ID join Team t on p.TeamID = t.ID
			   where p.FirstName = @firstName and  p.LastName = @lastName and a.YearInSchool = @year 
			   and a.Gender = @gender and t.SchoolName = @team)) begin
		print'Athlete records already exist in the DataBase';
		return 0
	end

	--Insert into Person table
	insert into Person (FirstName, LastName, TeamID)
	values (@firstName, @lastName, (select ID from Team where SchoolName = @team))

	--Get the ID to insert
	declare @newAthleteID int = ident_current('Person')

	--Insert into Athlete table
	insert into Athlete (ID, YearInSchool, Gender)
	values (@newAthleteID, @year, @gender)

	return 0
end

--exec AddAthlete
--@firstName = 'Bowser', @lastName = 'Bowering', @year = 1, @team = 'Rose-Hulman'

--select * from Person
--select * from Athlete