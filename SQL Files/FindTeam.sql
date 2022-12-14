-- =============================================
-- Author:		Kyle Brownell
-- Create date: 4/21/2022
-- Description:	Finds a team in the Team table based on input parameters, if given.
-- =============================================
-- Edit date:   5/17/2022
-- Description: Allows for admins to find based on all teams
CREATE PROCEDURE FindTeam(@teamName varchar(20) = null, @location varchar(50) = null)
AS
BEGIN
	IF(@teamName <> 'All') BEGIN
		IF(NOT (@teamName IS NULL OR EXISTS (SELECT * FROM Team WHERE SchoolName = @teamName))) BEGIN
			RAISERROR('ERROR: No teams with that name are found.', 14, 1);
			return 1;
		END
	END
	
	IF(NOT (@location IS NULL OR EXISTS (SELECT * FROM Team WHERE [Location] = @location))) BEGIN
		RAISERROR('ERROR: No teams found with specified home location', 14, 1);
		return 2;
	END
	

	--reads the team table based on parameters
	IF(@teamName = 'All') BEGIN
		SELECT *
		FROM Team 
	END
	ELSE BEGIN
		SELECT *
		FROM Team 
		WHERE @teamName IS NULL OR  SchoolName = @teamName AND 
			  @location IS NULL OR [Location] = @location
	END

	RETURN 0
END


--EXEC FindTeam @teamName = 'All'