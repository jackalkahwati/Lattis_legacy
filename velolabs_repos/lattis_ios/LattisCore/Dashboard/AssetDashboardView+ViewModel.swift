

import Combine
import Model
import SwiftUI

extension AssetDashboardView {
    
    @MainActor
    final class ViewModel: ObservableObject {
        
        @Published var card: Card = .none {
            willSet {
                lastCard = card
            }
        }
        @Published var alert: Alert?
        
        let asset: Asset
        weak var deletage: DashboardDelegate?
        weak var map: MapRepresentable?
        var lastCard: Card?
        
        fileprivate let initialState: Card
        
        init(_ asset: Asset, delegate: DashboardDelegate, map: MapRepresentable?, card: Card) {
            self.asset = asset
            self.deletage = delegate
            self.initialState = card
            self.map = map
            
            prepareMap()
        }
        
        func goBack() {
            map?.removeAllPoints()
            deletage?.didChange(status: .search, info: nil, animated: true)
        }
        
        func animateAppearance() {
            card = initialState
        }
        
        fileprivate func prepareMap() {
            map?.removeAllPoints()
            let asset = self.asset
            if let parking = Parking.Spot(asset) {
                DispatchQueue.main.asyncAfter(deadline: .now() + 1) { [weak self] in
                    self?.map?.add(points: [parking], selected: nil)
                    self?.map?.focus(on: asset.point.coordinate)
                }
            }
        }
    }
    
    enum Card {
        case none
        case booking(Booking)
        case rental(Trip)
        case parking(Parking.Spot)
        case summary(Trip)
        case dismiss
    }
    
    enum Alert: Identifiable, Equatable {
        case equipment(EquipmentControler.Alert)
        
        var id: Int {
            switch self {
            case .equipment:
                return 0
            }
        }
        
        static func == (lhs: AssetDashboardView.Alert, rhs: AssetDashboardView.Alert) -> Bool {
            lhs.id == rhs.id
        }
    }
}

extension Asset {
    var point: MapPoint {
        switch self {
        case .bike(let bike):
            return bike
        case .hub(let hub):
            return hub
        case .port(_ , let hub):
            return hub
        }
    }
}

extension Parking.Spot {
    init?(_ asset: Asset) {
        let hub: Hub
        switch asset {
        case .bike:
            return nil
        case .hub(let h):
            hub = h
        case .port(_, let h):
            hub = h
        }
        self.spotId = Int.random(in: (0...100000))
        self.name = hub.hubName
        self.latitude = hub.latitude
        self.longitude = hub.longitude
        self.details = hub.description
        self.pic = hub.imageURL
        self.kind = .generic
    }
}

extension AssetDashboardView.Card: Equatable {
    static func == (lhs: AssetDashboardView.Card, rhs: AssetDashboardView.Card) -> Bool {
        switch (lhs, rhs) {
        case (.rental, .rental):
            return true
        case (.booking, .booking):
            return true
        case (.none, .none):
            return true
        case (.summary, .summary):
            return true
        case (.dismiss, .dismiss):
            return true
        default:
            return false
        }
    }
}
