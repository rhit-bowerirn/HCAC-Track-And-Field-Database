create PROCEDURE ListTeamRankings
	-- Add the parameters for the stored procedure here
	@gender bit
AS
BEGIN
	IF(@gender is null)
	BEGIN
		RAISERROR('Gender cannot be null.',14,1)
		RETURN 1
	END
	IF(@gender = 1)
	BEGIN
		SELECT ROW_NUMBER() OVER(ORDER BY (t.MenPoints) DESC) AS Ranking, t.SchoolName, t.MenPoints AS Points
		FROM Team t
		ORDER BY t.MenPoints DESC
	END
	ELSE
	BEGIN

		SELECT ROW_NUMBER() OVER(ORDER BY (t.WomenPoints) DESC) AS Ranking, t.SchoolName, t.WomenPoints AS Points
		FROM Team t
		ORDER BY t.WomenPoints DESC
	END
	RETURN 0
END
