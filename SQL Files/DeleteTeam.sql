-- =============================================
-- Author:		Kyle Brownell and Braedyn Edwards
-- Create date: 4/21/2022
-- Description:	Removes a team in the Team table.
-- =============================================
-- Edit Date: 5/17/2022
-- Description: Removes reliance on teamID for teamName instead
CREATE PROCEDURE DeleteTeam(@teamName varchar(20))
AS
BEGIN
	IF(@teamName IS NULL)
	BEGIN
		RAISERROR('ERROR: Please provide a valid team name.', 14, 1)
		RETURN 1
	END

	IF(NOT EXISTS(SELECT SchoolName FROM Team WHERE SchoolName = @teamName))
	BEGIN
		RAISERROR('ERROR: Team does not exist in the database.', 14, 1)
		RETURN 2
	END

	DELETE Team
	WHERE SchoolName = @teamName

	RETURN 0
END
