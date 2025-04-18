import Fluent
import FluentMySQLDriver
import FluentMongoDriver
import Vapor

// configures your application
public func configure(_ app: Application) throws {
    // uncomment to serve files from /Public folder
    // app.middleware.use(FileMiddleware(publicDirectory: app.directory.publicDirectory))

    app.databases.use(.mysql(
        hostname: Environment.get("DATABASE_HOST") ?? "host",
        username: Environment.get("DATABASE_USERNAME") ?? "user",
        password: Environment.get("DATABASE_PASSWORD") ?? "password",
        database: "lattis_main",
        tlsConfiguration: nil
    ), as: .main)
    
    app.databases.use(.mysql(
        hostname: Environment.get("DATABASE_HOST_USERS") ?? "host",
        username: Environment.get("DATABASE_USERNAME") ?? "user",
        password: Environment.get("DATABASE_PASSWORD") ?? "password",
        database: "lattis_users",
        tlsConfiguration: nil
    ), as: .user)
    
//    app.versions = try .current()
    app.migrations.add(CreateOAuth(), to: .main)
    
    let mongo = try DatabaseConfigurationFactory.mongo(settings: .init(Environment.get("GPS_DB_URL") ?? "url"))
    app.databases.use(mongo, as: .gps)

    #if DEBUG
    fcm(app)
    #endif
    try routes(app)
}

extension DatabaseID {
    static var main: Self { .init(string: "lattis_main") }
    static var user: Self { .init(string: "lattis_users") }
    static var gps: Self { .init(string: "gps-data" )}
}

