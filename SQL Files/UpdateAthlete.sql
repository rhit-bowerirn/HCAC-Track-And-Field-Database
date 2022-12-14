create procedure UpdateAthlete(@originalFirstName varchar(20), @firstName varchar(20) = null, @originalLastName varchar(20), 
							   @lastName varchar(20) = null, @originalYear tinyint, @year tinyint = null, 
							   @originalTeam varchar(20), @team varchar(20) = null, @originalGender bit, 
							   @gender bit = null) as
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
		return 5
	end


	declare @teamID varchar(20)
	if(@team is not null) begin
		set @teamID = (select ID from Team where SchoolName = @team)
	end

	--get the ID of the athlete
	declare @athleteID int = (select p.ID from Person p join Athlete a on p.ID = a.ID join Team t on p.TeamID = t.ID
			   where p.FirstName = @originalFirstName and p.LastName = @originalLastName and a.YearInSchool = @originalYear 
			   and a.Gender = @originalGender and t.SchoolName = @originalTeam)

	--ensure the athlete actually exists
	if(@athleteID is null) begin
		raiserror('No records exist for this Athlete', 14, 1);
		return 6
	end

	--Update Person table
	update Person
	set FirstName = isNull(@firstName, FirstName), LastName = isNull(@lastName, LastName), TeamID =	isNull(@teamID, TeamID)
	where ID = @athleteID

	--Update Athlete Table
	update Athlete
	set YearInSchool = isNull(@year, YearInSchool), Gender = isNull(@gender, Gender)
	where ID = @athleteID

	return 0
end

