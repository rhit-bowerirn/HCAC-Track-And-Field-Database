create procedure FindCompetingTeams (@meetName varchar(50), @meetDate datetime, @meetLocation varchar(50)) as
begin
	--Validate parameters
	--Check for nulls
	if(@meetName is null) begin
		raiserror('Meet name cannot be null', 14, 1);
		return 1;
	end
	if(@meetDate is null) begin
		raiserror('Meet date cannot be null', 14, 1);
		return 2;
	end
	if(@meetLocation is null) begin
		raiserror('meet location cannot be null', 14, 1);
		return 3;
	end

	--Used to simplify the rest of the queries
	declare @meetID int = (select ID from Meet where [Name] = @meetName and day([Date]) = day(@meetDate) and [Location] = @meetLocation)

	--Check if the Meet exists
	if(@meetID is null) begin
		raiserror('No such meet exists', 14, 1);
		return 4;
	end
	
	--Read the Team table
	select t.SchoolName, t.[Location], t.MenPoints, t.WomenPoints
	from Team t
	join Competes c on c.TeamID = t.ID
	join Meet m on m.ID = c.MeetID
	where m.ID = @meetID

	return 0
end