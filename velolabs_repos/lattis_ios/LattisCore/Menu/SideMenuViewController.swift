//
//  SideMenuViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 18/04/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import SideMenu
import Cartography
import Localize_Swift
import Model

final class BageView: UIControl {
    
    fileprivate let label = UILabel.label(font: .theme(weight: .bold, size: .text), color: .white)
    
    init() {
        super.init(frame: .zero)
        
        addSubview(label)
        backgroundColor = .dodgerBlue
        layer.cornerRadius = 12
        isHidden = true
        label.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        
        constrain(label, self) { label, view in
            label.edges == view.edges.inseted(horizontally: 10, vertically: 2)
            view.height == 24
        }
    }
    
    var count: Int? {
        set {
            if let c = newValue, c != 0 {
                label.text = "\(c)"
                isHidden = false
            } else {
                label.text = nil
                isHidden = true
            }
        }
        get {
            guard let str = label.text else { return nil }
            return Int(str)
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

public final class SideMenuViewController: UIViewController {
    
    fileprivate let tableView = UITableView()
    fileprivate let profileView = MenuProfileView()
    fileprivate let logoView = UIImageView(image: UITheme.theme.logo)
    fileprivate var accountSection: MenuItem.Section = .init(title: "account".localized(), items: [
        .init(title: "payment".localized(), icon: .named("icon_billing"), module: { PaymentMethodsViewController(logic: .init()) }, navigation: true, hideMenu: true),
        .init(title: "memberships".localized(), icon: .named("icon_membership"), module: MembershipViewController.init, navigation: true, hideMenu: true),
        .init(title: "private_networks".localized(), icon: .named("icon_person"), module: PrivateNetworksViewController.init, navigation: true, hideMenu: true),
        .init(title: "ride_history".localized(), icon: .named("icon_history"), module: TripHistoryViewController.init, navigation: true, hideMenu: true),
    ])
    fileprivate var supportSection: MenuItem.Section = .init(title: "support".localized())
    fileprivate var logoutSection: MenuItem.Section = .init()
    fileprivate var sections: [MenuItem.Section] = []
    fileprivate var reservations: [Reservation] = [] {
        didSet {
            reservationsBage?.count = reservations.count
        }
    }
    fileprivate weak var reservationsBage: BageView?
    fileprivate var tripId: Int?
    fileprivate let network: ServiceNetwork & ReservationsNetwork & AppsAPI = AppRouter.shared.api()
    fileprivate let storage = UserStorage()
    fileprivate var info: Status.Info?
    fileprivate var status: Status = .search {
        didSet {
            switch status {
            case .trip(let model):
                self.tripId = model.trip.tripId
            default:
                self.tripId = nil
            }
            updateSupportSection()
            tableView.reloadData()
        }
    }

    override public func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }

        view.backgroundColor = .white
        view.addSubview(tableView)
        view.addSubview(logoView)
        logoView.contentMode = .scaleAspectFit
        
        logoutSection.items = [
            .init(title: "log_out".localized(), icon: .named("icon_logout"), module: AlertController.logOut, navigation: false, hideMenu: true)
        ]
        updateSupportSection()
        
        constrain(tableView, logoView, view) { table, logo, view in
            table.width == SideMenuController.preferences.basic.menuWidth
            table.top == view.safeAreaLayoutGuide.top
            table.bottom == logo.top - .margin
            table.right == view.right
            
            logo.left == table.left + .margin*2
            logo.right == view.right - .margin*2
            logo.bottom == view.safeAreaLayoutGuide.bottom - .margin/2
        }
        
        accountSection.items.insert(.init(title: "reservations".localized(), icon: .named("icon_reservation"), module: { ReservationsListViewController(self.reservations) }, navigation: true, hideMenu: true, identifier: "reservations"), at: 1)
        
        sections = [accountSection, supportSection, logoutSection]
        
        tableView.tableHeaderView = profileView
        tableView.tableFooterView = UIView()
        tableView.rowHeight = 54
        tableView.separatorStyle = .none
        tableView.register(SideMenuCell.self, forCellReuseIdentifier: "cell")
        tableView.register(SideMenuCell.self, forCellReuseIdentifier: "reservations")
        tableView.dataSource = self
        tableView.delegate = self
        
        profileView.photoButton.addTarget(self, action: #selector(changePhoto), for: .touchUpInside)
        profileView.addTarget(self, action: #selector(editProfile), for: .touchUpInside)
        
        AppRouter.shared.addPrivateNetwork = { [unowned self] in
            let fleets = PrivateNetworksViewController()
            fleets.shouldAddOne = true
            self.openModal(viewController: fleets)
        }
        
        AppRouter.shared.onInfoUpdate = { [unowned self] info, status in
            self.info = info
            self.status = status
            if let support = info?.supportPhone {
                Status.supportPhoneNumber = support
            }
        }
        
        storage.current(needRefresh: true) { [unowned self] (user) in
            self.profileView.user = user
        }
        
        fetchReservations()
        
        NotificationCenter.default.addObserver(self, selector: #selector(fetchReservations), name: .reservation, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(fetchReservations), name: .tripStarted, object: nil)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    func tripStarted(with tripId: Int) {
        self.tripId = tripId
        updateSupportSection()
        tableView.reloadData()
    }
    
    func tripEnded() {
        self.tripId = nil
        updateSupportSection()
        tableView.reloadData()
    }
    
    fileprivate func updateSupportSection() {
        supportSection.items = [
            .init(title: "help".localized(), icon: .named("icon_help"), module: {
                let phone = self.info?.phoneNumber ?? Status.supportPhoneNumber
                return HelpViewController(phone)
            }, navigation: true, hideMenu: true)
        ]
        if let damage = reportDamage() {
            supportSection.items = [
                .init(title: "report_damage".localized(), icon: .named("icon_report"), module: damage, navigation: true, hideMenu: false),
            ] + supportSection.items
        }
        
        network.fetchInfo { (result) in
            switch result {
            case .success(let info):
                Status.save(info: info)
            case .failure(let error):
                Analytics.report(error)
            }
        }
    }
    
    fileprivate func reportDamage() -> (() -> DamageViewController)? {
        switch self.status {
        case .booking(_, let bike):
            return {DamageViewController(bike.bikeId)}
        case .trip(let service):
            return {DamageViewController(service.bike.bikeId, tripId: service.trip.tripId)}
        default:
            return nil
        }
    }
    
    @objc
    fileprivate func fetchReservations() {
        network.fetchReservations { [weak self] (result) in
            switch result {
            case .success(let reservations):
                self?.reservations = reservations
                self?.reservationsBage?.count = reservations.count
            case .failure(let error):
                print(error)
            }
        }
    }
    
    fileprivate func reportTheft() {
        let report: Theft?
        switch self.status {
        case .trip(let trip):
            report = Theft(bikeId: trip.bike.bikeId, tripId: trip.trip.tripId)
        case .booking(let booking, let bike):
            report = Theft(bikeId: bike.bikeId, tripId: nil)
        default:
            report = nil
        }
        let alert = AlertController(title: "report_theft_title".localized(), message: .plain("report_theft_message".localized()))
        if let r = report {
            alert.actions = [
                .plain(title: "report_theft".localized()) { [unowned self] in
                    self.report(theft: r)
                },
                .cancel
            ]
        } else {
            alert.actions = [
                .plain(title: "ok".localized(), style: .active, handler: {
                    #warning("Report exeption")
                })
            ]
        }
        openModal(viewController: alert, navigation: false, hideMenu: false)
    }
    
    fileprivate func openModal(viewController: UIViewController, navigation: Bool = true, hideMenu: Bool = true) {
        if hideMenu {
            sideMenuController?.hideMenu()
        }
        let controller: UIViewController = navigation ? .navigation(viewController) : viewController
        sideMenuController?.contentViewController.present(controller, animated: true, completion: nil)
    }
    
    @objc fileprivate func handleMenuButtons(_ sender: MenuButton) {
        let module = sender.item.module()
        openModal(viewController: module, navigation: sender.item.navigation)
    }
    
    @objc fileprivate func changePhoto() {
        func picker(_ sourceType: UIImagePickerController.SourceType) {
            let pick = UIImagePickerController()
            pick.sourceType = sourceType
            pick.delegate = self
            pick.allowsEditing = true
            present(pick, animated: true, completion: nil)
        }
        let alert = AlertController(title: "Set profile picture", message: nil)
        if profileView.image != nil {
            alert.actions.append(.plain(title: "Delete Current", style: .delete) {
                self.profileView.image = nil
                })
        }
        if UIImagePickerController.isSourceTypeAvailable(.camera) {
            alert.actions.append(.plain(title: "Camera Roll") {
                picker(.camera)
                })
        }
        if UIImagePickerController.isSourceTypeAvailable(.photoLibrary) {
            alert.actions.append(.plain(title: "Library") {
                picker(.photoLibrary)
                })
        }
        alert.actions.append(.cancel)
        present(alert, animated: true, completion: nil)
    }
    
    @objc fileprivate func editProfile() {
        let profile = ProfileViewController()
        openModal(viewController: profile)
    }
    
    fileprivate func report(theft: Theft) {
        startLoading("theft_report_loader".localized())
        network.report(theft: theft) { [weak self] (result) in
            switch result {
            case .success:
                self?.theftReported()
            case .failure(let error):
                self?.handle(error)
            }
        }
    }
    
    fileprivate func theftReported() {
        switch status {
        case .trip(let service):
            if service.endTrip(parking: false, force: true) {
                startLoading("end_ride_loader".localized())
            }
//            service.endTrip(isTheft: true) { [weak self] (error, info, trip) in
//                if let error = error {
//                    self?.handle(error)
//                } else if let trip = trip {
//                    AppRouter.shared.dashboard?.didChange(status: .search, info: nil, animated: true)
//                    self?.stopLoading { self?.didEnd(trip: trip) }
//                } else {
//                    self?.warning()
//                }
//            }
        case .booking:
//            booking.tripService.disconnectLock()
            stopLoading {
                AppRouter.shared.dashboard?.didChange(status: .search, info: nil, animated: true)
                SideMenuViewController.theftReportedAlert(in: self.sideMenuController?.contentViewController)
            }
        default:
            break
        }
        sideMenuController?.hideMenu()
    }
    
    fileprivate func didEnd(trip: Trip) {
        let root = self.sideMenuController?.contentViewController
        let summary = TripSummaryViewController(trip) { [unowned root] in
            SideMenuViewController.theftReportedAlert(in: root)
        }
        root?.present(summary, animated: true, completion: nil)
    }
    
    fileprivate static func theftReportedAlert(in root: UIViewController?) {
        let alert = AlertController(title: "theft_report_success_title".localized(), message: .plain("theft_report_success_message".localized()))
        alert.actions.append(.ok)
        root?.dismiss(animated: true, completion: {
            root?.present(alert, animated: true, completion: nil)
        })
    }
}

extension SideMenuViewController: UITableViewDelegate, UITableViewDataSource {
    public func numberOfSections(in tableView: UITableView) -> Int {
        sections.count
    }
    
    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        sections[section].items.count
    }
    
    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let item = sections[indexPath.section].items[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: item.identifier, for: indexPath)
        if let c = cell as? SideMenuCell {
            c.item = item
            if item.identifier == "reservations" {
                reservationsBage = c.bageView
                reservationsBage?.count = reservations.count
            }
        }
        return cell
    }
    
    public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        let item = sections[indexPath.section].items[indexPath.row]
        let controller = item.module()
        openModal(viewController: controller, navigation: item.navigation)
    }
    
    public func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat {
        guard section < 2 else { return 0 }
        return 48
    }
    
    public func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        guard section < 2 else { return nil }
        let view = UIView()
        view.backgroundColor = .white
        let lineView = UIView.line
        lineView.backgroundColor = .lightGray
        view.addSubview(lineView)
        constrain(lineView, view) { line, view in
            line.centerY == view.centerY
            line.left == view.left + .margin
            line.right == view.right - .margin
        }
        return view
    }
    
    public func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        guard sections[section].title != nil else { return 0 }
        return 48
    }
    
    public func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        guard let title = sections[section].title else { return nil }
        let view = UIView()
        view.backgroundColor = .white
        let titleLabel = UILabel.label(text: title, font: .theme(weight: .medium, size: .text), color: .black)
        view.addSubview(titleLabel)
        constrain(titleLabel, view) { title, view in
            title.edges == view.edges.inseted(horizontally: .margin)
        }
        return view
    }
}

extension SideMenuViewController: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    public func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        dismiss(animated: true, completion: nil)
    }
    
    public func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        dismiss(animated: true, completion: nil)
        guard let image = info[.editedImage] as? UIImage else { return }
        profileView.image = image
    }
}

public extension UIViewController {
    @objc func menu() {
        sideMenuController?.revealMenu()
    }
}

extension Status.Info {
    var phoneNumber: String? {
        return operatorPhone ?? supportPhone
    }
}

extension AlertController {
    static func logOut() -> AlertController {
        let alert = AlertController(title: "log_out_title".localized(), message: .plain("log_out_message".localized()))
        alert.actions = [
            .plain(title: "confirm".localized(), handler: AppRouter.shared.logOut),
            .cancel
        ]
        return alert
    }
}


