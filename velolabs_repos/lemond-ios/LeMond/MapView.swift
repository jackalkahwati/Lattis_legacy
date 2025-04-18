//
//  MapView.swift
//  LeMond
//
//  Created by Ravil Khusainov on 27.12.2021.
//

import SwiftUI
import MapKit

struct MapView: View {
    
    @EnvironmentObject var viewModel: MapViewModel
    
    var body: some View {
        ZStack {
            Map(coordinateRegion: $viewModel.region, annotationItems: [viewModel.bike]) { annotation in
                MapAnnotation(coordinate: annotation.coordinate) {
                    ZStack {
                        Circle()
                            .fill(Color.ride)
                            .frame(width: 44, height: 44)
                            .overlay(
                                Circle()
                                    .strokeBorder(Color.white, lineWidth: 2)
                            )
                        Image(systemName: "bicycle")
                    }
                    .foregroundColor(.white)
                }
            }
        }
        .background(Color.background)
    }
}

struct MapView_Previews: PreviewProvider {
    static var previews: some View {
        MapView()
    }
}
