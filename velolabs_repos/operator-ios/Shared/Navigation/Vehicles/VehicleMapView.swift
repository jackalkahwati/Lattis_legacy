//
//  VehicleMapView.swift
//  Operator
//
//  Created by Ravil Khusainov on 13.04.2021.
//

import SwiftUI
import Combine

final class VehicleMapViewModel: ObservableObject {
    let vehicle: Vehicle
    @Published var annotations: [CustomMap.ValueAnnotation]
    @Published var selection: Vehicle?
    @Published var shouldFocusOnUser: Bool = false
    @Published var timeToRefresh: Bool = false
    fileprivate var refreshTimer: Timer!
    fileprivate var storage: Set<AnyCancellable> = []
    
    init(_ vehicle: Vehicle) {
        self.vehicle = vehicle
        self.annotations = [vehicle].compactMap(CustomMap.ValueAnnotation.init)
        startTimer()
    }
    
    deinit {
        refreshTimer.invalidate()
    }
    
    func refresh() {
        timeToRefresh = false
        refreshTimer.invalidate()
        CircleAPI.location(vehicleId: vehicle.id)
            .sink { [unowned self]  result in
                switch result {
                case .failure(let error):
                    print(error)
                case .finished:
                    startTimer()
                }
            } receiveValue: { [unowned self] coordinate in
                annotations = [.init(vehicle, coordinate: coordinate)]
            }
            .store(in: &storage)
    }
    
    private func startTimer() {
        refreshTimer = Timer.scheduledTimer(withTimeInterval: 10, repeats: true, block: { [weak self] _ in
            self?.timeToRefresh = true
        })
    }
}

struct VehicleMapView: View {
    
    @StateObject var viewModel: VehicleMapViewModel

    
    var body: some View {
        ZStack {
            CustomMap(
                annotations: $viewModel.annotations,
                focusSubject: .annotations,
                reFocus: $viewModel.shouldFocusOnUser
            )
            .ignoresSafeArea()
            VStack {
                Spacer()
                HStack {
                    if viewModel.timeToRefresh {
                        Button(action: viewModel.refresh) {
                            Text("refresh")
                        }
                        .buttonStyle(CreateButtonStyle())
                    }
                    Spacer()
                    Button(action: { viewModel.shouldFocusOnUser = true }, label: {
                        Image(systemName: "location.fill")
                            .padding(10)
                    })
                    .background(Circle().fill(Color.white))
                    .padding()
                }
            }
            .padding()
        }
        .navigationTitle(viewModel.vehicle.name)
    }
}


extension View {
    func snapshot() -> UIImage {
        let controller = HostingController(rootView: self)
        let view = controller.view
        view?.backgroundColor = .red

        let targetSize = controller.view.intrinsicContentSize
        view?.bounds = CGRect(origin: .zero, size: targetSize)
        
        let renderer = UIGraphicsImageRenderer(size: targetSize)

        return renderer.image { _ in
            view?.drawHierarchy(in: controller.view.bounds, afterScreenUpdates: true)
        }
    }
}

final class HostingController<Content: View>: UIHostingController<Content> {
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()

        view.setNeedsUpdateConstraints()
    }
}

extension UIImage {
    static func swiftUI<Content: View>(_ label: () -> Content) -> UIImage {
        label().snapshot()
    }
}

protocol MapItemProtocol {
    var image: UIImage { get }
}

struct MapItem<Content: View>: MapItemProtocol {
    let content: () -> Content
    
    var image: UIImage { .swiftUI(content)}
}

