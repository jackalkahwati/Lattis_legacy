
import Vapor
import Fluent

final class Fleet: Model {
    static var schema: String = "fleets"
    
    @ID(custom: "fleet_id")
    var id: Int?
    
    @Field(key: "fleet_name")
    var name: String?
    
    @Field(key: "type")
    var type: String
    
    @OptionalField(key: "logo")
    var logo: String?
    
    @OptionalField(key: "t_and_c")
    var legal: String?
    
    @OptionalField(key: "key")
    var key: String?
    
    @OptionalParent(key: "address_id")
    var address: Address?
    
    @OptionalChild(for: \.$fleet)
    var paymentSettings: PaymentSettings?
    
    @Children(for: \.$fleet)
    var vehicles: [Bike]
}

extension Fleet {
    final class Association: Model {
        static var schema: String = "fleet_associations"
        
        @ID(custom: "fleet_association_id")
        var id: Int?
        
        @Parent(key: "fleet_id")
        var fleet: Fleet
        
        @Field(key: "operator_id")
        var operatorId: Int?
    }
    
    struct Content: Vapor.Content {
        let id: Int
        let name: String?
        let type: String
        let logo: String?
        let legal: String?
        let address: Address?
        let vehiclesCount: Int
        
        init(_ model: Fleet) {
            self.id = model.id!
            self.name = model.name
            self.type = model.type
            self.logo = model.logo
            self.legal = model.legal
            self.address = model.address
            self.vehiclesCount = model.vehicles.filter({$0.status != .deleted && ($0.name == nil ? false : !$0.name!.isEmpty)}).count
        }
    }
    
    final class PaymentSettings: Model, Vapor.Content {
        
        static var schema: String = "fleet_payment_settings"
        
        @ID(custom: "id")
        var id: Int?
        
        @Parent(key: "fleet_id")
        var fleet: Fleet
    }
}

extension Fleet.Content: Hashable {
    static func == (lhs: Fleet.Content, rhs: Fleet.Content) -> Bool {
        lhs.id == rhs.id
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }
}
