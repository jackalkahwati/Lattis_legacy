
import Vapor
import Fluent

struct FleetController: RouteCollection {
    func boot(routes: RoutesBuilder) throws {
        let fleets = routes.grouped("fleets")
        fleets.get(use: index)
        fleets.group(":id") { fleet in
            fleet.get(use: find)
        }
        fleets.get("statistics", use: statistics)
    }
    
    func index(req: Request) throws -> EventLoopFuture<[Fleet.Content]> {
        if let oper = req.auth.get(FleetOperator.self) {
            return Fleet.Association.query(on: req.db(.main))
                .filter(\.$operatorId == oper.id)
                .with(\.$fleet) {
                    $0.with(\.$address)
                    $0.with(\.$vehicles)
                }
                .all()
                .map({
                    $0.compactMap({
                        Fleet.Content($0.fleet)
                    }).noDuplicates()
                })
        }
        return Fleet.query(on: req.db(.main))
            .with(\.$address)
            .with(\.$vehicles)
            .all()
            .map({
                $0.map({
                    Fleet.Content($0)
                }).noDuplicates()
            })
    }
    
    func find(req: Request) throws -> EventLoopFuture<Fleet.Content> {
        guard let id = req.parameters.get("id", as: Int.self) else {
            throw Abort(.badRequest, reason: "No id specified")
        }
        return Fleet.query(on: req.db(.main))
            .filter(\.$id == id)
            .with(\.$address)
            .with(\.$vehicles)
            .first()
            .unwrap(or: Abort(.notFound, reason: "No fleet with \(id)"))
            .map(Fleet.Content.init)
    }
    
    func statistics(req: Request) throws -> EventLoopFuture<String> {
        Fleet.query(on: req.db(.main))
            .filter(\.$name != nil)
            .with(\.$vehicles)
            .all()
            .map { fleets in
                fleets.map({$0.name! + ",\($0.vehicles.filter({$0.status != .deleted}).count)"}).joined(separator: "\n")
            }
    }
}

extension Array where Element: Hashable {
    func noDuplicates() -> Self {
        Self(Set(self))
    }
}

