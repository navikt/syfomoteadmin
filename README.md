# Syfomoteadmin
Previously named moteadmin, handles 'dialogmøter'-data.

## Local setup
The application can be started locally by running `LocalApplication.main`. It listens on port 8999.
A local instance of an in-memory H2 database is initialized during application startup. Queries can
be executed against `localhost/h2` by logging in using jdbc_url: `jdbc:h2:mem:testdb`. The database
is also available in test scope.

## Deployment
Build and deploy pipeline-jobs are located in: https://jenkins-digisyfo.adeo.no/job/digisyfo/job/syfomoteadmin/
