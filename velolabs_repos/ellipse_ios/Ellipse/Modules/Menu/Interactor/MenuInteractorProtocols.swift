//
//  MenuMenuInteractorProtocols.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

enum MenuItem: String {
    case home = "title_activity_home"
    case ellipses
    case find = "find_my_ellipse"
    case sharing
    case profile = "action_profile_settings"
    case emergency = "action_emergency_contacts"
    case help = "action_help"
    case order = "action_order_ellipse"
    case terms = "action_terms_conditions"
    case logout = "log_out"
}

extension MenuItem {
    var icon: UIImage {
        switch self {
        case .home:
            return UIImage(named: "menu_home")!
        case .ellipses:
            return UIImage(named: "menu_home")!
        case .profile:
            return UIImage(named: "menu_profile")!
        case .find:
            return UIImage(named: "menu_location")!
        case .sharing:
            return UIImage(named: "menu_sharing")!
        case .emergency:
            return UIImage(named: "menu_contacts")!
        case .order:
            return UIImage(named: "menu_order")!
        case .help:
            return UIImage(named: "menu_help")!
        case .terms:
            return UIImage(named: "menu_terms")!
        case .logout:
            return UIImage(named: "menu_logout")!
        }
    }
}

protocol MenuInteractorInput: TermsAndConditionsDelegate, TableViewPresentable {
    var terms: TermsInteractorOutput? {get set}
    func item(for indexPath: IndexPath) -> MenuItem
    func didSelect(item: MenuItem)
    func logOut()
}

protocol MenuInteractorOutput: class {

}
