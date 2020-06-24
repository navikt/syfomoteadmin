# Syfomoteadmin
Previously named moteadmin, handles 'dialogmøter'-data.

## Local setup
The application can be started locally by running `LocalApplication.main`. It listens on port 8999.
A local instance of an in-memory H2 database is initialized during application startup. Queries can
be executed against `localhost/h2` by logging in using jdbc_url: `jdbc:h2:mem:testdb`. The database
is also available in test scope.

## Pipeline

Pipeline is run with Github Actions.
Commits to Master-branch are deployed automatically to dev-sbs and prod-sbs.
Commits to non-master-branch is built with automatic deploy.

## Download packages form Github Package Registry
Certain packages must be downloaded from Github Package Registry, which requires authentication.
The packages can be downloaded via build.gradle:
```
val githubUser: String by project
val githubPassword: String by project
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/navikt/tjenestespesifikasjoner")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
}
```

`githubUser` and `githubPassword` are properties that are set in `~/.gradle/gradle.properties`:

```
githubUser=x-access-token
githubPassword=<token>
```

Where `<token>` is a personal access token with scope `read:packages`(and SSO enabled).

The variables can alternatively be configured as environment variables or used in the command lines:

* `ORG_GRADLE_PROJECT_githubUser`
* `ORG_GRADLE_PROJECT_githubPassword`

```
./gradlew -PgithubUser=x-access-token -PgithubPassword=[token]
```