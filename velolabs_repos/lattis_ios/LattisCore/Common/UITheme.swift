//
//  UITheme.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 02.03.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Model

public struct UITheme {
    internal init(logo: UIImage?, searchLogo: UIImage?, color: UITheme.Color, font: UITheme.Font, support: UITheme.Support, loadingImage: UIImage?, userAgent: String, displayName: String, termsOfService: String, privacyPolicy: String, paymentGateway: Payment.Gateway = .stripe, strict: [String] = []) {
        self.logo = logo
        self.searchLogo = searchLogo
        self.color = color
        self.font = font
        self.support = support
        self.loadingImage = loadingImage
        self.userAgent = userAgent
        self.displayName = displayName
        self.termsOfService = termsOfService
        self.privacyPolicy = privacyPolicy
        self.paymentGateway = paymentGateway
        self.strictTNC = strict
    }
    
    let logo: UIImage?
    let searchLogo: UIImage?
    let color: Color
    let font: Font
    let support: Support
    let loadingImage: UIImage?
    let userAgent: String
    let displayName: String
    let termsOfService: String
    let privacyPolicy: String
    let paymentGateway: Payment.Gateway
    let strictTNC: [String]
    
    public static var theme: UITheme = .lattis
}

public extension UITheme {
    static let lattis = UITheme(logo: .named("lattis_logo_color"),
                                searchLogo: nil,
                                color: .lattis,
                                font: .gotham,
                                support: .lattis,
                                loadingImage: .named("image_loading_bottom"),
                                userAgent: "lattis",
                                displayName: "Lattis",
                                termsOfService: "https://lattis.io/pages/terms-of-use",
                                privacyPolicy: "https://lattis.io/pages/privacy-policy")
    
    static let veloTransit = UITheme(logo: .named("velo_transit_logo_color"),
                                     searchLogo: .named("logo_find_ride_bike"),
                                     color: .veloTransit,
                                     font: .gotham,
                                     support: .veloTransit,
                                     loadingImage: .named("image_loading_bottom"),
                                     userAgent: "velo_transit",
                                     displayName: "Vélo-Transit",
                                     termsOfService: "https://lattis.io/pages/terms-of-use",
                                     privacyPolicy: "https://lattis.io/pages/privacy-policy")
    
    static let sandyPedals = UITheme(logo: nil,
                                     searchLogo: nil,
                                     color: .sandyPedals,
                                     font: .gotham,
                                     support: .lattis,
                                     loadingImage: .named("image_loading_bottom"),
                                     userAgent: "sandy_pedals",
                                     displayName: "Sandy Pedals",
                                     termsOfService: "https://s3.amazonaws.com/appforest_uf/f1621287653156x375732136633266640/SPB%20—%20Terms.docx.pdf",
                                     privacyPolicy: "https://s3.amazonaws.com/appforest_uf/f1621287644668x495055402602324600/SPB%20—%20Privacy.docx.pdf")
    
    static let guestbike = UITheme(logo: .named("guestbike_logo_color"),
                                   searchLogo: .named("logo_find_ride_gb"),
                                   color: .guestbike,
                                   font: .gotham,
                                   support: .lattis,
                                   loadingImage: nil,
                                   userAgent: "guestbike",
                                   displayName: "Guestbike",
                                   termsOfService: "https://guestbike.com/disclaimer/",
                                   privacyPolicy: "https://guestbike.com/disclaimer/")
    
    static let goscoot = UITheme(logo: .named("go_scoot_launch"),
                                 searchLogo: nil,
                                 color: .goscoot,
                                 font: .gotham,
                                 support: .lattis,
                                 loadingImage: .named("image_loading_bottom"),
                                 userAgent: "goscoot",
                                 displayName: "GoScoot",
                                 termsOfService: "https://secureservercdn.net/50.62.195.83/377.960.myftpupload.com/wp-content/uploads/2021/05/GO-SCOOT-TERMS-AND-CONDITION-APP-May-3-2021.pdf",
                                 privacyPolicy: "https://secureservercdn.net/50.62.195.83/377.960.myftpupload.com/wp-content/uploads/2021/05/go-scoot-Privacy-Policy_210503.pdf")
    
    static let giraff = UITheme(logo: .named("giraff_logo"),
                                searchLogo: nil,
                                color: .lattis,
                                font: .gotham,
                                support: .lattis,
                                loadingImage: .named("image_loading_bottom"),
                                userAgent: "giraff",
                                displayName: "Giraff",
                                termsOfService: "https://lattis.io/pages/terms-of-use",
                                privacyPolicy: "https://peritaly.hu/privacy/")
    
    static let wawe = UITheme(logo: .named("logo_welcome_wawe"),
                              searchLogo: .named("logo_search_wawe"),
                                color: .wawe,
                                font: .gotham,
                                support: .lattis,
                                loadingImage: nil,
                                userAgent: "wawe",
                                displayName: "WAWE",
                                termsOfService: "https://lattis.io/pages/terms-of-use",
                                privacyPolicy: "https://peritaly.hu/privacy/")
    
    static let wave = UITheme(logo: .named("logo_welcome_wave"),
                              searchLogo: .named("search_wave"),
                              color: .wave,
                              font: .gotham,
                              support: .lattis,
                              loadingImage: nil,
                              userAgent: "wave",
                              displayName: "Wave",
                              termsOfService: "https://lattis.io/pages/terms-of-use",
                              privacyPolicy: "https://peritaly.hu/privacy/")
    
    static func grin(agent: String = "grin") -> UITheme {
        .init(logo: .named("logo_welcome_grin"),
              searchLogo: nil,
              color: .grin,
              font: .gotham,
              support: .lattis,
              loadingImage: nil,
              userAgent: agent,
              displayName: agent == "grin" ? "Grin Lima" : "Grin Santiago",
              termsOfService: agent == "grin" ? "https://content.grow.mobi/p/pe/terms" : "https://content.grow.mobi/p/cl/terms",
              privacyPolicy: agent == "grin" ? "https://content.grow.mobi/p/pe/privacy" : "https://content.grow.mobi/p/cl/privacy",
              paymentGateway: .mercadopago,
              strict: [
                "He leido y estoy de acuerdo con los terminos y condiciones de la web.",
                "Tras leer la politica de privacidad, autorizo el tratamiento de mis datos personales para los propositos especificados en ella."
              ])
    }
    
    static let ulimitedBikingMobility = UITheme(logo: .named("Ub_welcome_page_logo"),
                                                searchLogo: .named("search_unlimited_biking"),
                                                color: .ulimitedBikingMobility,
                                                font: .gotham,
                                                support: .lattis,
                                                loadingImage: nil,
                                                userAgent: "unlimited-biking",
                                                displayName: "Unlimited Biking Micromobility",
                                                termsOfService: "https://lattis.io/pages/terms-of-use",
                                                privacyPolicy: "https://peritaly.hu/privacy/")
    
    static let mount = UITheme(logo: .named("mount_welcome_logo"),
                              searchLogo: nil,
                                color: .mount,
                                font: .gotham,
                                support: .lattis,
                                loadingImage: nil,
                                userAgent: "mount",
                                displayName: "Mount Mobility",
                                termsOfService: "https://lattis.io/pages/terms-of-use",
                                privacyPolicy: "https://peritaly.hu/privacy/")
    
    static let monkeyDonkey = UITheme(logo: .named("icon_monkey_welcome"),
                                      searchLogo: .named("search_monkey_donkey"),
                                      color: .monkeyDonkey,
                                      font: .gotham,
                                      support: .lattis,
                                      loadingImage: nil,
                                      userAgent: "monkey-donkey",
                                      displayName: "Monkey Donkey",
                                      termsOfService: "https://monkeydonkey.bike/tos",
                                      privacyPolicy: "https://monkeydonkey.bike/privacy")
    
    static let bandWagon = UITheme(logo: .named("welcome_logo_band_wagon"),
                                   searchLogo: .named("search_bandwagon"),
                                   color: .bandWagon,
                                   font: .gotham,
                                   support: .lattis,
                                   loadingImage: nil,
                                   userAgent: "bandwagon",
                                   displayName: "BandWagon Rentals",
                                   termsOfService: "https://bandwagmag.com/terms-of-use/",
                                   privacyPolicy: "https://bandwagmag.com/privacy-policy-for-bandwagon-scooters/")
    
    static let ourBike = UITheme(logo: .named("welcome_logo_our_bike"),
                                 searchLogo: .named("logo_search_ourbike"),
                                color: .ourBike,
                                font: .gotham,
                                support: .ourBike,
                                loadingImage: nil,
                                userAgent: "ourbike",
                                displayName: "OurBike",
                                termsOfService: "https://ourbike.co.uk/assets/uploads/files/terms-and-conditions.pdf",
                                privacyPolicy: "https://ourbike.co.uk/assets/uploads/files/privacy-policy.pdf")
    
    static let fin = UITheme(logo: .named("welcome_logo_fin"),
                             searchLogo: .named("fin_search_logo"),
                             color: .fin,
                             font: .gotham,
                             support: .fin,
                             loadingImage: nil,
                             userAgent: "fin",
                             displayName: "Fin",
                             termsOfService: "https://findf.in/rental-agreement/",
                             privacyPolicy: "https://findf.in/privacy-policy/")
    
    static let hooba = UITheme(logo: .named("hooba_logo"),
                               searchLogo: nil,
                               color: .hooba,
                               font: .gotham,
                               support: .hooba,
                               loadingImage: nil,
                               userAgent: "hooba",
                               displayName: "HOOBA",
                               termsOfService: "https://hooba.eu/Terms-and-Conditions/",
                               privacyPolicy: "https://hooba.eu/Privacy-Policy/")
    
    static let yryde = UITheme(logo: .named("yryde_logo"),
                               searchLogo: .named("yryde_search_logo"),
                               color: .yryde,
                               font: .gotham,
                               support: .yryde,
                               loadingImage: nil,
                               userAgent: "yryde",
                               displayName: "YRyde",
                               termsOfService: "https://www.yrydebykes.com/terms-and-conditions",
                               privacyPolicy: "https://www.yrydebykes.com")
    
    static let blade = UITheme(logo: .named("blade_logo"),
                               searchLogo: .named("blade_search_logo"),
                               color: .blade,
                               font: .gotham,
                               support: .blade,
                               loadingImage: nil,
                               userAgent: "blade",
                               displayName: "Blade Mobility",
                               termsOfService: "https://www.yrydebykes.com/terms-and-conditions",
                               privacyPolicy: "https://www.yrydebykes.com")
    
    static let pacificRides = UITheme(logo: .named("pacificrides_logo"),
                                      searchLogo: .named("search_logo_pacific"),
                                      color: .pacificRides,
                                      font: .gotham,
                                      support: .pacificRides,
                                      loadingImage: nil,
                                      userAgent: "pacificrides",
                                      displayName: "Pacific Rides",
                                      termsOfService: "https://www.yrydebykes.com/terms-and-conditions",
                                      privacyPolicy: "https://www.yrydebykes.com")
    
    static let trip = UITheme(logo: .named("trip_bikes_logo"),
                                      searchLogo: .named("search_logo_trip_bike"),
                                      color: .trip,
                                      font: .gotham,
                                      support: .trip,
                                      loadingImage: nil,
                                      userAgent: "trip",
                                      displayName: "TRIP Bikes",
                                      termsOfService: "https://www.ridetrip.com/terms-of-service",
                                      privacyPolicy: "https://www.ridetrip.com/privacy-policy")
    
    static let greenRiders = UITheme(logo: .named("green-riders-logo"),
                                      searchLogo: .named("green-riders-search"),
                                      color: .greenRiders,
                                      font: .gotham,
                                      support: .greenRiders,
                                      loadingImage: nil,
                                      userAgent: "greenriders",
                                      displayName: "Green-Riders",
                                      termsOfService: "https://green-riders.fr/content/2-mentions-legales",
                                      privacyPolicy: "https://green-riders.fr/content/2-mentions-legales")
    
    static let falcoSmart = UITheme(logo: .named("falco-smart-logo"),
                                      searchLogo: .named("falco_search_icon"),
                                      color: .falcoSmart,
                                      font: .gotham,
                                      support: .falcoSmart,
                                      loadingImage: nil,
                                      userAgent: "falcosmart",
                                      displayName: "FalcoSmart",
                                      termsOfService: "https://rentals.falco.co.uk/terms-and-conditions.html",
                                      privacyPolicy: "https://rentals.falco.co.uk/privacy.html")
    
    static let twr = UITheme(logo: .named("twr-logo"),
                                      searchLogo: .named("twr-search-icon"),
                                      color: .twr,
                                      font: .gotham,
                                      support: .twr,
                                      loadingImage: nil,
                                      userAgent: "twowheelrental",
                                      displayName: "TWR - Serving The Explorer",
                                      termsOfService: "https://38da5e79-712f-46d4-807c-bbb7b2aceaeb.filesusr.com/ugd/0078b7_0d2b4bea0a6c4682aeaf17effff34195.pdf",
                                      privacyPolicy: "https://38da5e79-712f-46d4-807c-bbb7b2aceaeb.filesusr.com/ugd/0078b7_fd2f116544334f0b8fda38d432ba0e9e.docx?dn=White%20Label%20Privacy%20Policy%20Template.DOCX")
    
    static let rockvelo = UITheme(logo: .named("rock-velo-logo"),
                                      searchLogo: .named("rock-velo-search"),
                                      color: .rockvelo,
                                      font: .gotham,
                                      support: .rockvelo,
                                      loadingImage: nil,
                                      userAgent: "rockvelo",
                                      displayName: "RockVelo",
                                      termsOfService: "https://www.rockvelo.com/terms-and-conditions/",
                                      privacyPolicy: "https://www.rockvelo.com/terms-and-conditions/")
    
    static let thrive = UITheme(logo: .named("thrive-logo"),
                                      searchLogo: .named("thrive-search"),
                                      color: .thrive,
                                      font: .gotham,
                                      support: .thrive,
                                      loadingImage: nil,
                                      userAgent: "thriveryde",
                                      displayName: "Thrive Ryde",
                                      termsOfService: "https://thriveryde.com/terms/",
                                      privacyPolicy: "https://thriveryde.com/terms/")
    
    static let lockem = UITheme(logo: .named("lockem-logo"),
                                searchLogo: .named("lockem-search"),
                                color: .lockem,
                                font: .gotham,
                                support: .lockem,
                                loadingImage: nil,
                                userAgent: "lockem",
                                displayName: "LOCKEM",
                                termsOfService: "https://cdn.website-editor.net/s/6852d6c357704495ae92740468821bce/files/uploaded/Terms%2520and%2520Conditions.pdf?Expires=1659062724&Signature=FBKO7JMejzOnGfZgBaoBeZHeu5HMjGuoVQjtxxVUCXobjm617I-RL8qBeaGO5X3FYmNRefkCri9sFiWZd6qAi4JOab8u4Dd4WRyoB1J4qqWILayoGDRupkSJQz3aA9zGuVnt3SBAOvefpgIYJ9gcEem131tZxZ9WmeI6vChK2Gni0S7sriL7lujImEH3JZvsYAEXcpRFKUuMFO~cNnsjqW7EUm9WFvsJN5OzXkKKmVXykpKI7YjvWLIMbDxb5xpWyqAhtdQMZ1NGFNB5C4LdPXxNXWQ6S667p32EDrB9rGuUMFkcRbuOEWwj6sffqLhwvOx-mjtHGDsxxpxveOz5qw__&Key-Pair-Id=K2NXBXLF010TJW",
                                privacyPolicy: "https://cdn.website-editor.net/s/6852d6c357704495ae92740468821bce/files/uploaded/Privacy%2520Policy.pdf?Expires=1659062724&Signature=hKY93jUcXiXC84JNAdBra9RU7uANGOXxh~5FD1nAMNfdvB3wh7760DayZ97Wgey0ZwpyyzleFzguIlwPXLXMLANgUTdS5eN-vAkpiVn9BEjwpO1nLiKxmLT5avMpgUnXuh7xFu7cGuVP9SJyTzfkjv1JhAeBHtJr0a6ssqzcltWDnokzj9v~QIwYvOSgrzrplwxWLVUV9iEEFIat5ThhlsFio2JA4LzQ7PFRC7Y3hdXiF4RK-pBcQOKAJGV1B7KMp3ZePVFaA1m7CZBOf-eHMx2dU6nRyoyXKFW~1SmMhJ1UrwX-Yde5BbcE4ZdQjYHHqEoGAb08CpgoxdzrccZwQQ__&Key-Pair-Id=K2NXBXLF010TJW")

    static let robyn = UITheme(logo: .named("robyn_logo"),
                                      searchLogo: .named("robyn_search"),
                                      color: .robyn,
                                      font: .gotham,
                                      support: .robyn,
                                      loadingImage: nil,
                                      userAgent: "robyn",
                                      displayName: "Robyn Scooters",
                                      termsOfService: "https://www.robynscooters.com/read-our-terms-and-conditions--xterms",
                                      privacyPolicy: "https://www.robynscooters.com/read-our-privacy-statement--xprivacy")
    
    struct Color {
        let main: UIColor
        let accent: UIColor
        let background: UIColor
        let secondaryBackground: UIColor
        let tint: UIColor
        let accentTint: UIColor
        let warning: UIColor
    }
    
    struct Font {
        let family: String
        
        public enum Weight: String {
            case medium = "Medium"
            case bold = "Bold"
            case light = "Light"
            case book = "Book"
            case bookItalic = "BookItalic"
            case boldItalic = "BlackItalic"
        }
        
        public enum Size: CGFloat {
            case tiny = 10
            case small = 12
            case text = 14
            case body = 16
            case title = 18
            case giant = 24
            case mighty = 48
        }
    }
    
    struct Support {
        let phoneNumber: String
        let email: String
        let faq: String?
    }
    
    var legal: String { "welcome_terms_and_privacy_text".localizedFormat(displayName, termsOfService, privacyPolicy) }
}

extension UITheme.Color {
    static let lattis = UITheme.Color(main: .red, accent: .black, background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let veloTransit = UITheme.Color(main: .red, accent: UIColor(red: 0.486, green: 0.761, blue: 0.259, alpha: 1), background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let sandyPedals = UITheme.Color(main: .red, accent: UIColor(red: 164.0/255.0, green: 52.0/255.0, blue: 54.0/255.0, alpha: 1.00), background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let guestbike = UITheme.Color(main: .red, accent: UIColor(red: 60.0/255.0, green: 219.0/255.0, blue: 192.0/255.0, alpha: 1), background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let goscoot = UITheme.Color(main: .red, accent: UIColor(red: 0.0/255.0, green: 156.0/255.0, blue: 204.0/255.0, alpha: 1), background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let giraff = UITheme.Color(main: .red, accent: UIColor(red: 35.0/255.0, green: 31.0/255.0, blue: 32.0/255.0, alpha: 1), background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let wawe = UITheme.Color(main: .red, accent: UIColor(red: 32.0/255.0, green: 105.0/255.0, blue: 168.0/255.0, alpha: 1), background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let wave = UITheme.Color(main: .red, accent: UIColor(red: 39.0/255.0, green: 187.0/255.0, blue: 195.0/255.0, alpha: 1), background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let grin = UITheme.Color(main: .red, accent: UIColor(red: 12.0/255.0, green: 216.0/255.0, blue: 112.0/255.0, alpha: 1), background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let ulimitedBikingMobility = UITheme.Color(main: .red, accent: UIColor(red: 106.0/255.0, green: 193.0/255.0, blue: 78.0/255.0, alpha: 1), background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let mount = UITheme.Color(main: .red, accent: UIColor(red: 50.0/255.0, green: 57.0/255.0, blue: 78.0/255.0, alpha: 1), background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let monkeyDonkey = UITheme.Color(main: .red, accent: UIColor(red: 53.0/255.0, green: 49.0/255.0, blue: 97.0/255.0, alpha: 1), background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let bandWagon = UITheme.Color(main: .red, accent: UIColor(red: 196.0/255.0, green: 69.0/255.0, blue: 70.0/255.0, alpha: 1), background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let ourBike = UITheme.Color(main: .red, accent: UIColor(red: 12.0/255.0, green: 139.0/255.0, blue: 68.0/255.0, alpha: 1), background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let fin = UITheme.Color(main: .red, accent: UIColor(red: 245.0/255.0, green: 132.0/255.0, blue: 31.0/255.0, alpha: 1), background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let hooba = UITheme.Color(main: .red, accent: UIColor(hexString: "0080FF")!, background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let yryde = UITheme.Color(main: .red, accent: UIColor(hexString: "0A70B9")!, background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let blade = UITheme.Color(main: .red, accent: UIColor(hexString: "EA3323")!, background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let pacificRides = UITheme.Color(main: .red, accent: UIColor(hexString: "557AF2")!, background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let trip = UITheme.Color(main: .red, accent: UIColor(hexString: "99D735")!, background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let greenRiders = UITheme.Color(main: .red, accent: UIColor(hexString: "00748B")!, background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let falcoSmart = UITheme.Color(main: .red, accent: UIColor(hexString: "197FCD")!, background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let twr = UITheme.Color(main: .red, accent: UIColor(hexString: "1DC001")!, background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let rockvelo = UITheme.Color(main: .red, accent: UIColor(hexString: "0EBF7F")!, background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let thrive = UITheme.Color(main: .red, accent: UIColor(hexString: "D02329")!, background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let lockem = UITheme.Color(main: .red, accent: UIColor(hexString: "4DCECE")!, background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
    
    static let robyn = UITheme.Color(main: .red, accent: UIColor(hexString: "1FCECB")!, background: .white, secondaryBackground: UIColor(red: 0.961, green: 0.961, blue: 0.961, alpha: 1), tint: UIColor(red: 0.713, green: 0.713, blue: 0.713, alpha: 1), accentTint: .white, warning: .red)
}

extension UITheme.Font {
    static let gotham = UITheme.Font(family: "GothamSSm")
}

public extension UITheme.Support {
    static let lattis = UITheme.Support(phoneNumber: "415-503-9744", email: "help@lattis.io", faq: nil)
    static let veloTransit = UITheme.Support(phoneNumber: "438-831-3805", email: "support@velotransit.ca", faq: nil)
    static let fin = UITheme.Support(phoneNumber: "877-523-9555", email: "paddle@finpaddleshare.com", faq: nil)
    static let ourBike = UITheme.Support(phoneNumber: "7958190306", email: "ourbike@peddlemywheels.com", faq: nil)
    static let hooba = UITheme.Support(phoneNumber: "+32471016365", email: "management@hooba.eu", faq: nil)
    static let yryde = UITheme.Support(phoneNumber: "562-888-0011", email: "yrydebykes@gmail.com", faq: nil)
    static let blade = UITheme.Support(phoneNumber: "561-377-0555", email: "blade@blade.me", faq: nil)
    static let pacificRides = UITheme.Support(phoneNumber: "1 (323) 815-4593", email: "info@pacificrides.io", faq: nil)
    static let trip = UITheme.Support(phoneNumber: "614.500.3223", email: "info@ridetrip.com", faq: nil)
    static let greenRiders = UITheme.Support(phoneNumber: "+33 01 48 20 90 49", email: "app-contact@green-riders.fr", faq: "https://green-riders.fr/content/4-contact")
    static let falcoSmart = UITheme.Support(phoneNumber: "01538380080", email: "bryan.duggan@falco.co.uk", faq: nil)
    static let twr = UITheme.Support(phoneNumber: "574-370-3828", email: "twowheelrental@outlook.com", faq: nil)
    static let rockvelo = UITheme.Support(phoneNumber: "+38640675604", email: "info@rockvelo.com", faq: nil)
    static let thrive = UITheme.Support(phoneNumber: "8333033666", email: "we@thriveryde.com", faq: nil)
    static let lockem = UITheme.Support(phoneNumber: "07958042237", email: "foyshal@lockem.co.uk", faq: nil)
    static let robyn = UITheme.Support(phoneNumber: "7153798417", email: "Carolynemiller1985@gmail.com", faq: nil)
}

public extension UIFont {
    static func theme(weight: UITheme.Font.Weight, size: UITheme.Font.Size) -> UIFont {
        UIFont(name: UITheme.theme.font.family + "-" + weight.rawValue, size: size.rawValue)!
    }
}

import SwiftUI

extension Font {
    static func theme(weight: UITheme.Font.Weight, size: UITheme.Font.Size) -> Font {
        .init(UIFont.theme(weight: weight, size: size) as CTFont)
    }
}
