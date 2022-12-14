create procedure [dbo].[ListEventResults] (@meetName varchar(50), @meetDate datetime, @meetLocation varchar(50), @eventName varchar(50)) as
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
		raiserror('Meet location cannot be null', 14, 1);
		return 3;
	end
	if(@eventName is null) begin
		raiserror('Event name cannot be null', 14, 1);
		return 4;
	end
	
	--Used to simplify the rest of the queries
	declare @meetID int = (select ID from Meet where [Name] = @meetName and day([Date]) = day(@meetDate) and [Location] = @meetLocation)
	declare @eventID int = (select ID from [Event] where [Name] = @eventName)
	declare @eventType varchar = (select [Type] from Event where [Name] = @eventName)

	--Check that the Meet exists
	if(@meetID is null) begin
		raiserror('No such meet exists', 14, 1);
		return 5;
	end
	--Check that the Event exists
	if(@eventID is null) begin
		raiserror('No such event exists', 14, 1);
		return 6;
	end
	if(not exists (select * from Offers where MeetID = @meetID and EventID = @eventID)) begin
		declare @errorMessage varchar = formatMessage('%s does not offer %s', @meetName, @eventName)
		raiserror(@errorMessage, 14, 1);
		return 7;
	end

	--Read Person, Athlete, Team and Participates for Event results
	select p.FirstName, p.LastName, a.YearInSchool, (case a.Gender when 1 then 'M' else 'F' end) as 'Gender', t.SchoolName, pa.Score, pa.Points
	from Participates pa
	join Meet m on pa.MeetID = m.ID
	join [Event] e on pa.EventID = e.ID
	join Athlete a on pa.AthleteID = a.ID
	join Person p on a.ID = p.ID
	join Team t on p.TeamID = t.ID
	where pa.MeetID = @meetID and pa.EventID = @eventID
	order by pa.Points desc

	return 0
end