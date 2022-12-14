create Proc FindCoachesTeam (
	@username nvarchar(20)
)
AS
BEGIN
	
	--Validate parameters
	if(@username is null) begin
		RAISERROR('Username cannot be null', 14, 1);
		return 1;
	end
	
	--Check to see if user exists in database
	if(not exists(select * from [User] u where u.Username = @username)) begin
		RAISERROR('User must exist in database', 14, 1);
		return 2;
	end

	declare @personID int
	set @personID = (select u.PersonID from [User] u where u.Username = @username)
	

	--Gets team name
	select t.SchoolName
	from Person p
	join Team t on t.ID = p.TeamID
	join Coach c on c.ID = p.ID
	where p.ID = @personID
	print @personID
	return 0;
END

--Exec FindCoachesTeam @username = 'ab'