import Fluent
import Vapor

func routes(_ app: Application) throws {
    app.get("health", use: { $0.eventLoop.makeSucceededFuture(HTTPStatus.ok) })
    try app.group("operator", configure: oper)
    try app.group("clip", configure: clip)
    try app.group("manager", configure: manager)
    try app.group("statistics", configure: statistics)
    #if DEBUG
    try app.group("debug", configure: notExposed)
    #endif
}

private func oper(routes: RoutesBuilder) throws {
    let authenticator = OperatorAuthenticator()
    routes.post("login", use: authenticator.login)
    let op = routes.grouped(HeaderAuthenticator()).grouped(authenticator)
    try op.register(collection: OperatorController())
    try op.register(collection: TicketController())
    try op.register(collection: BikeController())
    try op.register(collection: LockController())
    try op.register(collection: FleetController())
    try op.register(collection: RoutesDebugController())
    try op.register(collection: ThingController())
    try op.register(collection: HubController())
    try op.register(collection: TripController())
    try op.register(collection: MapController())
    try op.register(collection: BookingController())
    try op.register(collection: UserController())
}

private func clip(routes: RoutesBuilder) throws {
    let authenticator = ClipAuthenticator()
    try routes.register(collection: ClipVehiclesController())
    let clip = routes.grouped(authenticator)
    clip.post("signin", use: authenticator.signIn)
}

private func manager(routes: RoutesBuilder) throws {
    
}

private func statistics(routes: RoutesBuilder) throws {
    let stat = routes.grouped(StatisticsAuthenticator())
    try stat.register(collection: StatisticsController())
}

private func notExposed(routes: RoutesBuilder) throws {
    try routes.register(collection: FCMDebugController())
}

