create procedure ListPBsByAthlete(@firstName varchar(20), @lastName varchar(20), @year tinyint, @team varchar(20), @gender bit) as
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

	if(not exists (select * from Person p join Athlete a on p.ID = a.ID join Team t on p.TeamID = t.ID
		where p.FirstName = @firstName and  p.LastName = @lastName and a.YearInSchool = @year 
		and a.Gender = @gender and t.SchoolName = @team)) begin
		raiserror('No records found for this Athlete', 14, 1);
		return 6
	end
	
	--Used to simplify the rest of the queries
	declare @athleteID int = (select a.ID from Athlete a join Person p on a.ID = p.ID join Team t on p.TeamID = t.ID 
							  where p.FirstName = @firstName and p.LastName = @lastName and a.YearInSchool = @year 
							  and a.Gender = @gender and t.SchoolName = @team)

	select * from AthleteScoreSheet(@athleteID);

end

