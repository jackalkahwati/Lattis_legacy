//
//  MapWrapper.swift
//  Operator
//
//  Created by Ravil Khusainov on 05.05.2021.
//

import MapKit
import Combine
import SwiftUI
import CoreLocation

protocol MapWrapperConfiguration {
    func update(bbox: BBox)
}

struct MapWrapper<AnnotationType: Equatable>: UIViewRepresentable {
    init(configuration: MapWrapperConfiguration? = nil,
        annotations: Binding<[PointAnnotation]>,
         selection: Binding<AnnotationType?>,
         focusOn: Binding<Bool> = .init(get: {false}, set: {_ in}),
         annotationContent: ((PointAnnotation) -> MapItemProtocol?)? = nil,
         clusterContent: ((Int) -> MapItemProtocol?)? = nil) {
        self.configuration = configuration
        self._annotations = annotations
        self._selection = selection
        self._focusOnUser = focusOn
        self.annotationContent = annotationContent
        self.clusterContent = clusterContent
    }
    
    @Binding var annotations: [PointAnnotation]
    @Binding var selection: AnnotationType?
    @Binding var focusOnUser: Bool
    let configuration: MapWrapperConfiguration?
    let annotationContent: ((PointAnnotation) -> MapItemProtocol?)?
    let clusterContent: ((Int) -> MapItemProtocol?)?
    
    func makeUIView(context: Context) -> MKMapView {
        let map = MKMapView()
        context.coordinator.configure(mapView: map)
        
        if !annotations.isEmpty && context.coordinator.isInitialized {
            reloadAnnotations(map: map)
            context.coordinator.needRecenter = false
            map.showAnnotations(annotations, animated: false)
        }
        return map
    }
    
    func updateUIView(_ uiView: MKMapView, context: Context) {
        if focusOnUser {
            focusOnUser = false
            uiView.setCenter(uiView.userLocation.coordinate, animated: true)
            return
        }
        guard context.coordinator.shouldReload, context.coordinator.isInitialized else {
            context.coordinator.shouldReload = true
            return
        }
        reloadAnnotations(map: uiView)
        if context.coordinator.needRecenter {
            context.coordinator.needRecenter = false
            uiView.showAnnotations(annotations, animated: true)
        }
    }
    
    func makeCoordinator() -> Coordinator {
        .init(self)
    }
    
    func reloadAnnotations(map: MKMapView) {
        let old = map.annotations.compactMap({$0 as? PointAnnotation})
        if !Set(old).subtracting(Set(annotations)).isEmpty {
            map.removeAnnotations(map.annotations)
        }
        map.addAnnotations(annotations)
    }
    
    func onMove(completion: @escaping (BBox) -> Void) -> MapWrapper {
    
        return self
    }
    
    final class AnnotationView: MKAnnotationView {
        init(annotation: PointAnnotation, content: ((PointAnnotation) -> MapItemProtocol?)?) {
            super.init(annotation: annotation, reuseIdentifier: "annotation")
            clusteringIdentifier = "cluster"
            image = content?(annotation)?.image
        }
        
        required init?(coder aDecoder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }
    }
    
    final class ClusterView: MKAnnotationView {
        init(annotation: MKClusterAnnotation, content: ((Int) -> MapItemProtocol?)?) {
            super.init(annotation: annotation, reuseIdentifier: "cluster")
            image = content?(annotation.memberAnnotations.count)?.image
        }
        
        required init?(coder aDecoder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }
    }
    
    final class Coordinator: NSObject, MKMapViewDelegate {
        let map: MapWrapper
        var shouldReload = true
        var isInitialized = false
        var needRecenter = true
        let manager = CLLocationManager()
        
        init(_ map: MapWrapper) {
            self.map = map
            super.init()
        }
        
        func configure(mapView: MKMapView) {
            manager.requestWhenInUseAuthorization()
            let tap = UITapGestureRecognizer(target: self, action: #selector(deselectAnnotation))
            mapView.addGestureRecognizer(tap)
            
            mapView.delegate = self
            mapView.showsUserLocation = true
        }
        
        @objc
        func deselectAnnotation() {
            map.selection = nil
        }
        
        func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
            if let view = mapView.view(for: annotation), annotation is MKUserLocation {
                view.canShowCallout = false
            }
            if let a = annotation as? MKClusterAnnotation {
                return ClusterView(annotation: a, content: map.clusterContent)
            } else if let point = annotation as? PointAnnotation {
                return AnnotationView(annotation: point, content: map.annotationContent)
            }
            return nil
        }
        
        func mapView(_ mapView: MKMapView, didSelect view: MKAnnotationView) {
            if let cluster = view.annotation as? MKClusterAnnotation {
                mapView.showAnnotations(cluster.memberAnnotations, animated: true)
            }
            else if let point = view.annotation as? PointAnnotation, let sel = point.vehicle as? AnnotationType {
                shouldReload = false
                mapView.setCenter(point.coordinate, animated: true)
                map.selection = nil
                map.selection = sel
            }
        }
        
        func mapView(_ mapView: MKMapView, didUpdate userLocation: MKUserLocation) {
            guard needRecenter else { return }
            needRecenter = false
            let camera = MKMapCamera(lookingAtCenter: userLocation.coordinate, fromEyeCoordinate: userLocation.coordinate, eyeAltitude: 1000)
            mapView.setCamera(camera, animated: false)
        }
        
        func mapViewDidFinishLoadingMap(_ mapView: MKMapView) {
            if !isInitialized {
                isInitialized = true
                map.reloadAnnotations(map: mapView)
            }
        }
        
        func mapView(_ mapView: MKMapView, regionDidChangeAnimated animated: Bool) {
            map.configuration?.update(bbox: .init(rect: mapView.visibleMapRect))
        }
        
//        func mapViewDidChangeVisibleRegion(_ mapView: MKMapView) {
//            map.configuration?.update(bbox: .init(rect: mapView.visibleMapRect))
//        }
    }
}

final class PointAnnotation: NSObject, MKAnnotation {
    let vehicle: Vehicle
    let coordinate: CLLocationCoordinate2D
    
    
    init(_ vehicle: Vehicle, coordinate: CLLocationCoordinate2D) {
        self.vehicle = vehicle
        self.coordinate = coordinate
    }
}

extension PointAnnotation {
    convenience init?(_ vehicle: Vehicle) {
        guard let coordinate = vehicle.coordinate else { return nil }
        self.init(vehicle, coordinate: coordinate)
    }
}

extension BBox {
    init(rect: MKMapRect) {
        let ne = MKMapPoint(x: rect.maxX, y: rect.minY)
        let sw = MKMapPoint(x: rect.minX, y: rect.maxY)
        self.init(ne: ne.coordinate, sw: sw.coordinate)
    }
}
