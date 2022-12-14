CREATE FUNCTION TeamTotalPoints
(
	@teamID int,
	@gender bit
)
RETURNS smallint
AS
BEGIN
	DECLARE @teamPoints smallint

	SELECT @teamPoints = SUM(p.Points)
	FROM Participates p
	JOIN Person pe ON p.AthleteID = pe.ID
	JOIN Athlete a ON p.AthleteID = a.ID
	WHERE pe.TeamID = @teamID
	AND a.Gender = @gender

	IF(@teamPoints IS NULL)
	BEGIN
		SET @teamPoints = 0
	END
	RETURN @teamPoints

END
