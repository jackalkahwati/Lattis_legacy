
import Combine
import Model
import OvalBackend
import SwiftUI

extension AssetRentalView {
    
    @MainActor
    final class ViewModel: ObservableObject {
        
        @Published var isLoading: Bool = false
        @Published var price: String = ""
        @Published var time: String = ""
        @Published var lockAlert: EquipmentControler.Alert?
        @Published var thing: Thing?
        @Published var tripStarted: Bool
        @Published var canEndTrip: Bool = false
        @Published var hintMessage: String?
        @Binding var alert: AssetDashboardView.Alert?
        @Binding var card: AssetDashboardView.Card
        
        fileprivate var timer: Timer!
        fileprivate var invoice: Trip.Invoice!
        fileprivate let updateInterval: TimeInterval = 10
        fileprivate var duration: TimeInterval = 0
        fileprivate let oval = OvalBackend()
        fileprivate let map: MapObserver = .shared
        fileprivate let timeFormatter = DateComponentsFormatter()
        fileprivate var callbackStorage: AnyCancellable?
        fileprivate var storage: Set<AnyCancellable> = []
        fileprivate var warnings: [String] = [] {
            didSet {
                hintMessage = warnings.last
            }
        }
        
        internal init(trip: Trip, asset: Asset, alert: Binding<AssetDashboardView.Alert?>, card: Binding<AssetDashboardView.Card>) {
            self.trip = trip
            self.tripStarted = trip.isStarted
            self.asset = asset
            self._alert = alert
            self._card = card
            refreshEquipment()
            beginUpdate()
            observe()
            duration = trip.duration
            updateUI()
            Task {
                await refresh()
            }
        }
        
        fileprivate(set) var trip: Trip
        let asset: Asset
        
        var hintColor: Color {
            return tripStarted ? .blue : .red
        }
        
        func start() {
            isLoading = true
            Task { [unowned self] in
                do {
                    self.trip = try await oval.startTrip(with: asset)
                    self.tripStarted = true
                    self.refreshEquipment()
                } catch {
                    print(error)
                }
                isLoading = false
            }
        }
        
        func end() {
            struct End: Encodable {
                let tripId: Int
                let latitude: Double
                let longitude: Double
            }
            isLoading = true
            Task {
                do {
                    let trip = try await oval.end(trip: End(tripId: trip.tripId, latitude: 0, longitude: 0))
                    endUpdate()
                    self.card = .summary(trip)
                } catch {
                    print(error)
                }
                isLoading = false
            }
        }
        
        func conroller(thing: Thing) -> EquipmentControler {
            let controller = EquipmentControler(thing, asset: asset)
            callbackStorage = controller.callback
                .sink { result in
                    switch result {
                    case .failure(let error):
                        Analytics.report(error)
                    case .finished:
                        break
                    }
                } receiveValue: { [unowned self] value in
                    switch value {
                    case .alert(let alert):
                        self.alert = .equipment(alert)
                    case .show(let message):
                        warnings.append(message)
                    case .hide(let message):
                        if let idx = warnings.firstIndex(of: message) {
                            warnings.remove(at: idx)
                        }
                    default:
                        break
                    }
                    
                }
            return controller
        }
        
        fileprivate func observe() {
            map.selected
                .sink { [unowned self] point in
                    guard let spot = point as? Parking.Spot else { return }
                    self.card = .parking(spot)
                }
                .store(in: &storage)
        }
        
        fileprivate func refreshEquipment() {
            guard tripStarted else {
                warnings.append("booking_timer_expired_label".localized())
                return
            }
            thing = asset.equipment
            if let idx = warnings.firstIndex(of: "booking_timer_expired_label".localized()) {
                warnings.remove(at: idx)
            }
        }
        
        fileprivate func beginUpdate() {
            timer = .scheduledTimer(withTimeInterval: 1, repeats: true, block: { [weak self] (_) in
                self?.increment()
            })
        }
        
        fileprivate func endUpdate() {
            guard timer.isValid else { return }
            timer.invalidate()
        }
        
        fileprivate func increment() {
            duration += 1
            
            if Int(duration)%Int(updateInterval) == 0 {
                Task {
                    await refresh()
                }
            }
            updateUI()
        }
        
        fileprivate func refresh() async {
            do {
                let invoice = try await oval.update(trip: trip.upload([]))
                updateUI(invoice)
            } catch {
                print(error)
            }
        }
        
        fileprivate func updateUI(_ invoice: Trip.Invoice? = nil) {
            if let price = invoice?.price {
                self.price = price
            }
            if let dur = invoice?.duration {
                duration = dur
            }
            
            if duration < .minute {
                timeFormatter.allowedUnits = [.second]
                timeFormatter.unitsStyle = .short
            } else if duration < .hour {
                timeFormatter.allowedUnits = [.minute]
                timeFormatter.unitsStyle = .short
            } else {
                timeFormatter.allowedUnits = [.day, .hour, .minute]
                timeFormatter.unitsStyle = .positional
            }
            time = timeFormatter.string(from: duration) ?? "--:--"
        }
    }
}

extension Thing: Equatable {
    public static func == (lhs: Thing, rhs: Thing) -> Bool {
        lhs.controllerId == rhs.controllerId
    }
}
