-- =============================================
-- Author:		Kyle Brownell
-- Create date: 4/21/2022
-- Description:	Adds a team into the Team table.
-- =============================================
CREATE PROCEDURE AddTeam(@teamName varchar(20), @location varchar(50))
AS
BEGIN
	IF(@teamName IS NULL)
	BEGIN
		RAISERROR('ERROR: Team name cannot be null.', 14, 1)
		RETURN 1
	END

	IF(EXISTS(SELECT SchoolName FROM Team WHERE SchoolName = @teamName))
	BEGIN
		RAISERROR('ERROR: Team already exists with given name.', 14, 1)
		RETURN 2
	END

	IF(@location IS NULL)
	BEGIN
		RAISERROR('ERROR: Location name cannot be null.', 14, 1)
		RETURN 3
	END

	
	INSERT INTO Team (SchoolName, Location)
	VALUES(@teamName, @location)

	RETURN 0
END
