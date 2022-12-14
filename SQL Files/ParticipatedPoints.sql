CREATE FUNCTION ParticipatedPoints
(
	-- Add the parameters for the function here
	@meetID int,
	@athleteID int,
	@eventID int
)
RETURNS smallint
AS
BEGIN
	-- Declare the return variable here
	DECLARE @totalPoints smallint = 0,
			@eventType varchar(20),
			@gender bit
	
	SELECT @gender = a.Gender
	FROM Athlete a
	WHERE a.ID = @athleteID

	-- Add the T-SQL statements to compute the return value here
	IF(@eventType = 'Jump' OR @eventType = 'Throw')
	BEGIN
		SELECT @totalPoints = RowValues.RowNum
		FROM(
			SELECT ROW_NUMBER() OVER(ORDER BY MAX(p.Score) DESC) AS RowNum,
					p.AthleteID,
					p.EventID,
					MIN(p.MeetID) AS MeetID
			FROM Participates p
			JOIN Athlete a ON p.AthleteID = a.ID
			WHERE p.EventID = @eventID
			AND a.Gender = @gender
			GROUP BY p.AthleteID, p.EventID
			) AS RowValues
		WHERE RowValues.AthleteID = @athleteID AND
				RowValues.EventID = @eventID AND
				RowValues.MeetID = @meetID
	END
	ELSE
	BEGIN
		SELECT @totalPoints = RowValues.RowNum
		FROM(
			SELECT ROW_NUMBER() OVER(ORDER BY MIN(p.Score) ASC) AS RowNum,
					p.AthleteID,
					p.EventID ,
					MIN(p.MeetID) AS MeetID
			FROM Participates p
			JOIN Athlete a ON p.AthleteID = a.ID
			WHERE p.EventID = @eventID
			AND a.Gender = @gender
			GROUP BY p.AthleteID, p.EventID
			) AS RowValues
		WHERE RowValues.AthleteID = @athleteID AND
				RowValues.EventID = @eventID AND
				RowValues.MeetID = @meetID

	END
	IF(@totalPoints = 0)
	BEGIN
		SET @totalPoints = 0
	END
	ELSE IF(@totalPoints = 1)
	BEGIN
		SET @totalPoints = 10
	END
	ELSE IF(@totalPoints = 2)
	BEGIN
		SET @totalPoints = 8
	END
	ELSE IF(@totalPoints < 9)
	BEGIN
		SET @totalPoints = 9 - @totalPoints
	END
	ELSE
	BEGIN
		SET @totalPoints = 0
	END
	-- Return the result of the function
	RETURN @totalPoints

END
