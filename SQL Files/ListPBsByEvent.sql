create procedure ListPBsByEvent (@eventName varchar(50)) as
begin
	--Validate parameters
	--Check for nulls

	if(@eventName is null) begin
		raiserror('Event name cannot be null', 14, 1);
		return 1;
	end

	declare @eventID int = (select ID from [Event] where [Name] = @eventName)
	declare @eventType varchar(20) = (select [Type] from [Event] where [Name] = @eventName)

	--Check that the Event exists
	if(@eventID is null) begin
		raiserror('No such event exists', 14, 1);
		return 2;
	end

	if(@eventType = 'Jump' or @eventType = 'Throw') begin
		select p.FirstName, p.LastName, a.YearInSchool, (case a.Gender when 1 then 'M' else 'F' end) as 'Gender', t.SchoolName, max(pa.Score) as 'Mark'
		from Participates pa
		join [Event] e on pa.EventID = e.ID
		join Athlete a on a.ID = pa.AthleteID
		join Person p on p.ID = a.ID
		join Team t on t.ID = p.TeamID
		where e.ID = @eventID
		group by p.FirstName, p.LastName, a.YearInSchool, t.SchoolName, a.Gender
		order by max(pa.Score) desc
	end
	else begin
		select p.FirstName, p.LastName, a.YearInSchool, (case a.Gender when 1 then 'M' else 'F' end) as 'Gender', t.SchoolName, min(pa.Score) as 'Time'
		from Participates pa
		join [Event] e on pa.EventID = e.ID
		join Athlete a on a.ID = pa.AthleteID
		join Person p on p.ID = a.ID
		join Team t on t.ID = p.TeamID
		where e.ID = @eventID
		group by p.FirstName, p.LastName, a.YearInSchool, t.SchoolName, a.Gender
		order by min(pa.Score) asc
	end

	return 0
end

--Exec ListPBsByEvent
--@eventName = 'Test'