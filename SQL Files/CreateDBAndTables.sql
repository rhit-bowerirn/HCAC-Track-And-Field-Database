Create Database [S2G5Demo]
On
	Primary (Name=HCACTFDemoData, 
	FILENAME='D:\Database\MSSQL15.MSSQLSERVER\MSSQL\Data\HCACTF.mdf',
	SIZE = 6MB,
	MAXSIZE = 30MB,
	FILEGROWTH = 12%)
Log On
	(Name=HCACTFDemoLog,
	FILENAME='D:\Database\MSSQL15.MSSQLSERVER\MSSQL\DATA\HCACTF.ldf',
	SIZE = 3MB,
	MAXSIZE = 22MB,
	FILEGROWTH = 17%)

CREATE USER HCAC_Account FROM LOGIN HCAC_Account; exec sp_addrolemember 'db_owner', 'HCAC_Account'; 

go

use S2G5Demo
go
create table Team (
	ID int identity(0, 1),
	SchoolName varchar(20) unique not null,
	[Location] varchar(50) not null,
	primary key(ID)
);

create table Person (
	ID int identity(0, 1),
	FirstName varchar(20) not null,
	LastName varchar(20) not null,
	TeamID int not null,
	primary key(ID),
	foreign key(TeamID) references Team(ID)
);


create table Athlete (
	ID int,
	YearInSchool tinyint not null,
	Gender bit not null,
	primary key(ID),
	foreign key(ID) references Person(ID)
);


create table Coach (
	ID int,
	EventType varchar(10) not null,
	IsHeadCoach bit not null,
	primary key(ID),
	foreign key(ID) references Person(ID)
);

create table [Event] (
	ID int identity(0, 1),
	[Name] varchar(50) unique not null,
	[Type] varchar(20) not null,
	primary key(ID)
);

create table Meet (
	ID int identity(0, 1),
	[Name] varchar(50) unique not null,
	[Date] datetime unique not null,
	[Location] varchar(50) not null,
	primary key(ID)
);

create table Competes (
	TeamID int,
	MeetID int,
	primary key(TeamID, MeetID),
	foreign key(TeamID) references Team(ID),
	foreign key(MeetID) references Meet(ID)
);

create table Participates (
	AthleteID int,
	EventID int,
	MeetID int,
	[Score] varchar(8) not null,
	primary key(AthleteID, EventID, MeetID),
	foreign key(AthleteID) references Athlete(ID),
	foreign key(EventID) references [Event](ID),
	foreign key(MeetID) references Meet(ID)
);

create table [User] (
	ID int identity(0, 1),
	Email varchar(50) unique not null,
	Username nvarchar(50) unique not null,
	PasswordSalt varchar(50) not null,
	PasswordHash varchar(50) not null,
	PersonID int not null,
	[Type] varchar(8),
	primary key(ID),
	foreign key (PersonID) references Person(ID)
);

create table Offers (
	MeetID int,
	EventID int,
	primary key(MeetID, EventID),
	foreign key(MeetID) references Meet(ID),
	foreign key(EventID) references Event(ID)
);

