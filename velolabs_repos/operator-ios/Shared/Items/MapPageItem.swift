//
//  MapPageItem.swift
//  Operator
//
//  Created by Ravil Khusainov on 09.03.2021.
//

import SwiftUI
import MapKit

struct MapPageItem: View {
    
    let coordinate: CLLocationCoordinate2D
    fileprivate let annotations: [Ann]
    @State fileprivate var region: MKCoordinateRegion = .init()
    
    init(_ coordinate: CLLocationCoordinate2D) {
        self.coordinate = coordinate
        self.annotations = [.init(coordinate: coordinate)]
    }
    
    var body: some View {
        VStack {
            Map(coordinateRegion: $region, interactionModes: .all, showsUserLocation: true, annotationItems: annotations) { ann in
                MapPin(coordinate: ann.coordinate)
            }
            .cornerRadius(10)
            Spacer()
            Button(action: {}, label: {
                Text("Direction")
            })
            .disabled(true)
            .buttonStyle(CreateButtonStyle())
        }
        .padding([.leading, .bottom, .trailing])
        .onAppear(perform: recenter)
    }
    
    func recenter() {
        region = .init(center: coordinate, span: .init(latitudeDelta: 0.002, longitudeDelta: 0.002))
    }
}

extension MapPageItem {
    struct Ann: Identifiable {
        let id: UUID = .init()
        let coordinate: CLLocationCoordinate2D
    }
}

//struct MapPageItem_Previews: PreviewProvider {
//    static var previews: some View {
//        MapPageItem()
//    }
//}
