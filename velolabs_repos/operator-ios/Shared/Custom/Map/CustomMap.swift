//
//  CustomMap.swift
//  Operator
//
//  Created by Ravil Khusainov on 11.11.2021.
//

import MapKit
import SwiftUI
import Combine

struct CustomMap: UIViewRepresentable {
    
    static var cleanCache: Bool = false

    @Binding var annotations: [ValueAnnotation]
    @Binding var reFocus: Bool
    let onMove: Move
    let onSelect: Select
    let onDeselect: () -> Void
    let focusSubject: FocusSubject
    
    init(
        annotations: Binding<[ValueAnnotation]>,
        onMove: @escaping Move = {_ in},
        onSelect: @escaping Select = {_ in},
        onDeselect: @escaping () -> Void = {},
        focusSubject: FocusSubject = .user,
        reFocus: Binding<Bool> = .init(get: {false}, set: {_ in})
    ) {
            self._annotations = annotations
            self.onMove = onMove
            self.onSelect = onSelect
            self.onDeselect = onDeselect
            self.focusSubject = focusSubject
            self._reFocus = reFocus
        }

    func makeUIView(context: Context) -> MKMapView {
        let view = MKMapView()
        context.coordinator.configure(mapView: view)
        context.coordinator.update(annotations)
        return view
    }

    func updateUIView(_ uiView: MKMapView, context: Context) {
        if reFocus {
            reFocus = false
            context.coordinator.reFocus(mapView: uiView, animated: true, withZoom: false)
            return
        }
        context.coordinator.update(annotations)
    }

    func makeCoordinator() -> Coordinator {
        .init(onMove, onSelect: onSelect, onDeselect: onDeselect, focusSubject: focusSubject)
    }
}

extension CustomMap {
    
    typealias Move = (BBox) -> ()
    typealias Select = (ValueAnnotation) -> ()
    
    enum FocusSubject {
        case user
        case annotations
        case userAndAnnotations
    }
    
    final class Coordinator: NSObject {
        
        let onMove: Move
        let onSelect: Select
        let onDeselect: () -> Void
        let focusSubject: FocusSubject
        
        init(_ onMove: @escaping Move, onSelect: @escaping Select, onDeselect: @escaping () -> Void, focusSubject: FocusSubject) {
            self.onMove = onMove
            self.onSelect = onSelect
            self.onDeselect = onDeselect
            self.focusSubject = focusSubject
            super.init()
        }
        
        fileprivate var cache: [ValueAnnotation] = []
        fileprivate let show: PassthroughSubject<[ValueAnnotation], Never> = .init()
        fileprivate let delete: PassthroughSubject<[MKAnnotation], Never> = .init()
        fileprivate var lastKnownLocation: CLLocation?
        fileprivate let manager = CLLocationManager()
        fileprivate var storage: Set<AnyCancellable> = []
        fileprivate var initialFocus = false
        
        func configure(mapView: MKMapView) {
            manager.requestWhenInUseAuthorization()
            let tap = UITapGestureRecognizer(target: self, action: #selector(deselectAnnotation))
            mapView.addGestureRecognizer(tap)
            
            mapView.delegate = self
            mapView.showsUserLocation = true
            
            show
                .sink { [weak mapView, unowned self] annotations in
                    mapView?.addAnnotations(annotations)
                    self.cache += annotations
                }
                .store(in: &storage)
            
            delete
                .sink { [weak mapView] annotations in
                    mapView?.removeAnnotations(annotations)
                }
                .store(in: &storage)
        }
        
        func update(_ annotations: [ValueAnnotation]) {
            var showValue: [ValueAnnotation] = []
            var deleteValue: [ValueAnnotation] = []
            
            annotations.forEach { annotation in
                if let idx = self.cache.firstIndex(where: {$0.value.isEqual(to: annotation.value)}) {
                    let existing = self.cache[idx]
                    if existing.coordinate != annotation.coordinate {
                        deleteValue.append(existing)
                        showValue.append(annotation)
                        self.cache.remove(at: idx)
                    }
                } else {
                    showValue.append(annotation)
                }
            }
            
            if CustomMap.cleanCache {
                CustomMap.cleanCache.toggle()
                deleteValue = cache
                cache.removeAll()
            }
            
            if !deleteValue.isEmpty {
                delete.send(deleteValue)
            }
            if !showValue.isEmpty {
                show.send(showValue)
            }
        }
        
        @objc
        fileprivate func deselectAnnotation() {
            onDeselect()
        }
        
        fileprivate func reFocus(mapView: MKMapView, animated: Bool = true, withZoom: Bool = true) {
            var coordinates: [CLLocationCoordinate2D] = []
            switch focusSubject {
            case .user:
                guard let location = lastKnownLocation else {
                    return
                }
                if withZoom {
                    let camera = MKMapCamera(lookingAtCenter: location.coordinate, fromEyeCoordinate: location.coordinate, eyeAltitude: 1200)
                    mapView.setCamera(camera, animated: false)
                } else {
                    mapView.setCenter(location.coordinate, animated: true)
                }
                return
            case .annotations:
                coordinates = cache.map(\.coordinate)
            case .userAndAnnotations:
                coordinates = cache.map(\.coordinate)
                if let location = lastKnownLocation {
                    coordinates.append(location.coordinate)
                }
            }
            guard let first = coordinates.first else { return }
            if withZoom {
                let camera = MKMapCamera(lookingAtCenter: first, fromEyeCoordinate: first, eyeAltitude: 1200)
                mapView.setCamera(camera, animated: animated)
            } else {
                mapView.setCenter(first, animated: true)
            }
        }
    }
    
    final class PinView: MKAnnotationView {
        
        let ann: ValueAnnotation
                
        init(annotation: ValueAnnotation) {
            self.ann = annotation
            super.init(annotation: annotation, reuseIdentifier: "annotation")
            clusteringIdentifier = "cluster"
            let view = content().uiView
            addSubview(view)
            let size = view.systemLayoutSizeFitting(CGSize(width: .max, height: .max))
            let frm = CGRect(origin: .zero, size: size)
            frame = frm
            view.frame = frm
        }
        
        required init?(coder aDecoder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }
        
        @ViewBuilder
        fileprivate func content() -> some View {
            if let vehicle = ann.value as? Vehicle {
                VehicleMapItem(vehicle: vehicle)
            } else {
                EmptyView()
            }
        }
    }
    
    final class ClusterView: MKAnnotationView {
                
        init(annotation: MKClusterAnnotation) {
            super.init(annotation: annotation, reuseIdentifier: "cluster")
            let view = content().uiView
            addSubview(view)
            let size = view.systemLayoutSizeFitting(CGSize(width: .max, height: .max))
            let frm = CGRect(origin: .zero, size: size)
            frame = frm
            view.frame = frm
        }
        
        required init?(coder aDecoder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }
        
        fileprivate var count: Int {
            guard let ann = annotation as? MKClusterAnnotation else { return 0 }
            return ann.memberAnnotations.count
        }
        
        @ViewBuilder fileprivate func content() -> some View {
            MapClusterView(count: count)
        }
    }
    
    final class ValueAnnotation: NSObject, MKAnnotation {
        
        let value: AnnotationValue
        let coordinate: CLLocationCoordinate2D
        
        init(_ value: AnnotationValue, coordinate: CLLocationCoordinate2D) {
            self.value = value
            self.coordinate = coordinate
        }
    }
}

protocol AnnotationValue {
    func isEqual(to: AnnotationValue) -> Bool
}

extension CustomMap.Coordinator: MKMapViewDelegate {
    func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
        if let a = annotation as? MKClusterAnnotation {
            return CustomMap.ClusterView(annotation: a)
        } else if let ann = annotation as? CustomMap.ValueAnnotation {
            return CustomMap.PinView(annotation: ann)
        }
        return nil
    }
    
    func mapView(_ mapView: MKMapView, didSelect view: MKAnnotationView) {
        if let cluster = view.annotation as? MKClusterAnnotation {
            mapView.showAnnotations(cluster.memberAnnotations, animated: true)
        }
        else if let ann = view.annotation as? CustomMap.ValueAnnotation {
            mapView.setCenter(ann.coordinate, animated: true)
            onSelect(ann)
        }
    }
    
    func mapView(_ mapView: MKMapView, didUpdate userLocation: MKUserLocation) {
        let needReFocus = lastKnownLocation == nil// && focusSubject.in([.user, .userAndAnnotations])
        lastKnownLocation = userLocation.location
        if needReFocus {
            reFocus(mapView: mapView, animated: false)
        }
        if let view = mapView.view(for: userLocation), view.canShowCallout {
            view.isEnabled = false
        }
    }
    
    func mapViewDidFinishLoadingMap(_ mapView: MKMapView) {
//        mapView.showAnnotations(show.value, animated: true)
//        reFocus(mapViev: mapView)
        if !initialFocus {
            initialFocus.toggle()
            reFocus(mapView: mapView)
        }
    }
    
    func mapView(_ mapView: MKMapView, regionDidChangeAnimated animated: Bool) {
        onMove(.init(rect: mapView.visibleMapRect))
    }
}

final class CustomMapMockViewModel: ObservableObject {
    @Published var annotations: [CustomMap.ValueAnnotation] = []
    @Published var debugInfo: String = "Debug"
    fileprivate var storage: Set<AnyCancellable> = []
    
    func updated(_ bbox: BBox) {
        fetch(with: bbox)
    }
    
    func fetch(with bbox: BBox) {
        storage.forEach{$0.cancel()}
        CircleAPI.vehicles(map: bbox, fleetId: 55, filters: [])
            .map {
                $0.map(Vehicle.init)
            }
            .sink { [weak self] result in
//                self?.startTimer()
            } receiveValue: { [unowned self] vehicles in
                annotations = vehicles.compactMap(CustomMap.ValueAnnotation.init)
            }
            .store(in: &storage)
    }
}


struct CustomMap_Previews: PreviewProvider {
    @StateObject fileprivate static var viewModel = CustomMapMockViewModel()
    
    static var previews: some View {
        VStack {
            CustomMap(
                annotations: $viewModel.annotations,
                onMove: viewModel.updated
            )
            Text(viewModel.debugInfo)
        }
    }
}

extension Vehicle: AnnotationValue {
    func isEqual(to: AnnotationValue) -> Bool {
        guard let veh = to as? Vehicle else { return false }
        return veh.id == id
    }
}

extension CustomMap.ValueAnnotation {
    convenience init?(_ vehicle: Vehicle) {
        guard let lat = vehicle.metadata.latitude, let lon = vehicle.metadata.longitude else { return nil }
        self.init(vehicle, coordinate: .init(latitude: lat, longitude: lon))
    }
}

extension View {
    var uiView: UIView {
        let host = HostingController(rootView: self)
        host.view.backgroundColor = .clear
        return host.view
    }
}

