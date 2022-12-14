CREATE or ALTER   procedure [dbo].[FindCoach](@FName varchar(20) = null, @LName varchar(20) = null, @TeamName  varchar(20) = null) AS
BEGIN
	--Validate parameters
	--We actually don't need to check anything, we want to be able to find lists of Athletes with similar attributes
	--We can try to be helpful if the user enters an attribute that doesn't exist however
	if(not(@FName is null or @FName = '' or exists (select * from Person where FirstName = @FName))) begin
		print formatMessage('No coaches found with first name %s', @FName);
	end
	if(not(@LName is null or @LName = '' or exists (select * from Person where LastName = @LName))) begin
		print formatMessage('No coaches found with last name %s', @LName);
	end
	if(not(@TeamName is null or @TeamName = '' or not(@TeamName = 'All') or exists (select * from Person p join Team t on p.TeamID = t.ID where t.SchoolName = @TeamName))) begin
		print formatMessage('No coaches found on team %s', @TeamName);
	end
	
	if(@TeamName = 'All') begin
		select p.FirstName, p.LastName, t.SchoolName as TeamName, c.IsHeadCoach, c.EventType
		from Person p 
		join Coach c on p.ID = c.ID
		join Team t on p.TeamID = t.ID
		where @FName is null or @FName = '' or p.FirstName = @FName and 
		  @LName is null or @LName = '' or p.LastName = @LName and
		  @TeamName is null or @TeamName = '' or t.SchoolName = @TeamName
	end
	else begin
		select p.FirstName, p.LastName, t.SchoolName as TeamName, c.IsHeadCoach, c.EventType
		from Person p 
		join Coach c on p.ID = c.ID
		join Team t on p.TeamID = t.ID and t.SchoolName = @TeamName
		where @FName is null or @FName = '' or p.FirstName = @FName and 
		  @LName is null or @LName = '' or p.LastName = @LName and
		  @TeamName is null or @TeamName = '' or t.SchoolName = @TeamName
	end

	

	--Return 0 to indicate success
	return 0;	
END

--Exec FindCoach @FName = 'Larry', @LName = 'Cole', @TeamName = 'Rose-Hulman'