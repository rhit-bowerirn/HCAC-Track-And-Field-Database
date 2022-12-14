create procedure FindAthletes (@firstName varchar(20) = null, @lastName varchar(20) = null, @year tinyint = null, 
							   @team varchar(20) = null, @gender bit = null) as
begin
	--Validate parameters
	--We actually don't need to check anything, we want to be able to find lists of Athletes with similar attributes
	--We can try to be helpful if the user enters an attribute that doesn't exist however
	if(not(@firstName is null or @firstName = '' or exists (select * from Person where FirstName = @firstName))) begin
		print formatMessage('No athletes found with first name %s', @firstName);
	end
	if(not(@lastName is null or @lastName = '' or exists (select * from Person where LastName = @lastName))) begin
		print formatMessage('No athletes found with last name %s', @lastName);
	end
	if(not(@team is null or @team = '' or not(@team = 'All') or exists (select * from Person p join Team t on p.TeamID = t.ID where t.SchoolName = @team))) begin
		print formatMessage('No athletes found on team %s', @team);
	end
	if(not(@year is null or @year = '' or exists (select * from Athlete where YearInSchool = @year))) begin
		print formatMessage('No athletes found with %d years in school', @year);
	end
	--Don't need to check gender, there will definitely be data for both

	--ALL FOR ADMIN
	if(@team = 'All') begin
		select p.FirstName, p.LastName, a.YearInSchool,(case a.Gender when 1 then 'M' else 'F' end) as Gender , t.SchoolName
		from Person p 
		join Athlete a on p.ID = a.ID
		join Team t on p.TeamID = t.ID
	end
	else begin
		--Read the Athlete and Person tables
		select p.FirstName, p.LastName, a.YearInSchool, (case a.Gender when 1 then 'M' else 'F' end) as 'Gender'
		from Person p 
		join Athlete a on p.ID = a.ID
		join Team t on p.TeamID = t.ID and t.SchoolName = @team
		where @firstName is null or @firstName = '' or p.FirstName = @firstName and 
			  @lastName is null or @lastName = '' or p.LastName = @lastName and
			  @year is null or @year = '' or a.YearInSchool = @year and
			  @team is null or @team = '' or t.SchoolName = @team and
			  @gender is null or a.Gender = @gender

		return 0
	end
end


--Exec FindAthletes @team = 'Rose-Hulman'

