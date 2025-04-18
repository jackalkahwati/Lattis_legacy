//
//  VehicleThingDetailsView.swift
//  Operator
//
//  Created by Ravil Khusainov on 24.03.2021.
//

import SwiftUI

struct VehicleThingDetailsView: View {
    
    let vehicle: Vehicle
    @StateObject var logic: ThingDetailsLogicController
    
    var body: some View {
        VStack {
            ThingDetailView(logic: logic)
        }
    }
}

//struct VehicleThingDetailsView_Previews: PreviewProvider {
//    static var previews: some View {
//        VehicleThingDetailsView()
//    }
//}
