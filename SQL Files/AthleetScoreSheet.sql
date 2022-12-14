create function AthleteScoreSheet(@athleteID int)
returns @scoreSheet table([Event] varchar(50), [Type] varchar(20), Score varchar(8)) as
begin
	insert into @scoreSheet ([Event], [Type], Score)
		select e.[Name], e.[Type], min(pa.Score)
		from Participates pa		
		join Athlete a on a.ID = pa.AthleteID
		left join [Event] e on pa.EventID = e.ID
		where a.ID = @athleteID and not (e.[Type] = 'Jump' or e.[Type] = 'Throw')
		group by e.[Name], e.[Type]

	insert into @scoreSheet ([Event], [Type], Score)
		select e.[Name], e.[Type], max(pa.Score)
		from Participates pa		
		join Athlete a on a.ID = pa.AthleteID
		left join [Event] e on e.ID = pa.EventID
		where a.ID = @athleteID and (e.[Type] = 'Jump' or e.[Type] = 'Throw')
		group by e.[Name], e.[Type]

	return
end

