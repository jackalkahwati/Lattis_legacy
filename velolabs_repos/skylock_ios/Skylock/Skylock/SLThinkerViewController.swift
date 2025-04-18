//
//  SLThinkerViewController.swift
//  Skylock
//
//  Created by Andre Green on 7/20/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

enum SLThinkerViewControllerState {
    case clockwiseMoving
    case clockwiseStill
    case counterClockwiseMoving
    case counterClockwiseStill
    case inactive
    case connecting
}

enum SLThinkerViewControllerLabelTextState {
    case clockwiseTopStill
    case clockwiseTopMoving
    case clockwiseBottomStill
    case clockwiseBottomMoving
    case counterClockwiseTopStill
    case counterClockwiseTopMoving
    case counterClockwiseBottomStill
    case counterClockwiseBottomMoving
    case inactiveTop
    case inactiveBottom
    case connectingTop
    case connectingBottom
}

protocol SLThinkerViewControllerDelegate:class {
    func thinkerViewTapped(tvc: SLThinkerViewController)
}

class SLThinkerViewController: UIViewController {
    private var texts:[SLThinkerViewControllerLabelTextState:String]
    
    private let firstBackgroundColor:UIColor
    
    private let secondBackgroundColor:UIColor
    
    private let foregroundColor:UIColor

    private let inActiveBackgroundColor:UIColor
    
    private let textColor:UIColor
    
    private var currentBackgroundColor:UIColor?
    
    private var currentTintColor:UIColor?
    
    private let foregroundScaler:CGFloat = 0.8
    
    private let animationDuration:Double = 0.32
    
    private var shouldContinueAnimation:Bool = false
    
    private(set) var thinkerState:SLThinkerViewControllerState = .inactive
    
    private var hasBeenTapped:Bool = false
    
    weak var delegate:SLThinkerViewControllerDelegate?
    
    lazy var backgroundView:UIView = {
        let view:UIView = UIView(frame: self.view.bounds)
        view.backgroundColor = self.currentBackgroundColor
        view.layer.cornerRadius = self.viewRadius()
        
        
        return view
    }()
    
    lazy var wormView:UIView = {
        let view:UIView = UIView(frame: self.view.bounds)
        view.backgroundColor = UIColor.clear
        
        return view
    }()
    
    lazy var foregroundView:UIView = {
        let diameter = 2.0*self.foregroundScaler*self.viewRadius()
        let frame = CGRect(
            x: 0.5*(self.view.bounds.size.width - diameter),
            y: 0.5*(self.view.bounds.size.height - diameter),
            width: diameter,
            height: diameter
        )
        
        let tgr = UITapGestureRecognizer(target: self, action: #selector(viewTapped))
        tgr.numberOfTapsRequired = 1
        
        let view:UIView = UIView(frame: frame)
        view.backgroundColor = self.foregroundColor
        view.layer.cornerRadius = 0.5*diameter
        view.clipsToBounds = true
        view.isUserInteractionEnabled = true
        view.addGestureRecognizer(tgr)
        
        return view
    }()
    
    lazy var topLabel:UILabel = {
        let height:CGFloat = 23.0
        let frame = CGRect(
            x: 0.0,
            y: 0.5*self.foregroundView.bounds.size.height - height,
            width: self.foregroundView.bounds.size.width,
            height: height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 21.0)
        label.textAlignment = .center
        label.textColor = self.textColor
        return label
    }()
    
    lazy var bottomLabel:UILabel = {
        let height:CGFloat = 23.0
        let frame = CGRect(
            x: 0.0,
            y: 0.5*self.foregroundView.bounds.size.height,
            width: self.foregroundView.bounds.size.width,
            height: height
        )
        
        let label:UILabel = UILabel(frame: frame)
        label.font = UIFont(name: SLFont.MontserratRegular.rawValue, size: 21.0)
        label.textAlignment = .center
        label.textColor = self.textColor
        
        return label
    }()
    
    init(
        texts: [SLThinkerViewControllerLabelTextState:String],
        firstBackgroundColor: UIColor,
        secondBackgroundColor: UIColor,
        foregroundColor: UIColor,
        inActiveBackgroundColor: UIColor,
        textColor: UIColor
        )
    {
        self.texts = texts
        self.firstBackgroundColor = firstBackgroundColor
        self.secondBackgroundColor = secondBackgroundColor
        self.foregroundColor = foregroundColor
        self.inActiveBackgroundColor = inActiveBackgroundColor
        self.textColor = textColor
        
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        if !self.view.subviews.contains(self.backgroundView) {
            self.view.addSubview(self.backgroundView)
        }
        
        if !self.view.subviews.contains(self.wormView) {
            self.view.addSubview(self.wormView)
        }
        
        if !self.view.subviews.contains(self.foregroundView) {
            self.view.addSubview(self.foregroundView)
            self.foregroundView.addSubview(self.topLabel)
            self.foregroundView.addSubview(self.bottomLabel)
        }
    }
    
    private func viewRadius() -> CGFloat {
        return self.view.bounds.size.width < self.view.bounds.size.height ?
            0.5*self.view.bounds.size.width : 0.5*self.view.bounds.size.height
    }
    
    private func rotateWormView(shouldRotateClockwise: Bool) {
        UIView.animate(withDuration: self.animationDuration, delay: 0.0, options: .curveLinear, animations: {
            self.wormView.transform = self.wormView.transform.rotated(by: CGFloat(M_PI))
        }) { (finished) in
            if self.shouldContinueAnimation {
                self.rotateWormView(shouldRotateClockwise: shouldRotateClockwise)
            }
        }
    }
    
    private func updateWormLayer() {
        if let sublayers = self.wormView.layer.sublayers {
            for layer:CALayer in sublayers {
                layer.removeFromSuperlayer()
            }
        }
        
        let innerRadius = self.foregroundScaler*self.viewRadius()
        let halfArcHeight = 0.5*(self.viewRadius() - innerRadius)
        let startPoint = CGPoint(x: self.wormView.center.x, y: self.wormView.center.y - innerRadius)
        let center1 = CGPoint(x: self.wormView.center.x, y: halfArcHeight)
        let center2 = CGPoint(x: self.wormView.center.x, y: self.wormView.bounds.size.height - halfArcHeight)
        
        let bezierPath:UIBezierPath = UIBezierPath()
        bezierPath.move(to: startPoint)
        bezierPath.addArc(
            withCenter: center1,
            radius: halfArcHeight,
            startAngle: CGFloat(M_PI_2),
            endAngle: 3.0*CGFloat(M_PI_2),
            clockwise: true
        )
        bezierPath.addArc(
            withCenter: self.wormView.center,
            radius: self.viewRadius(),
            startAngle: -CGFloat(M_PI_2),
            endAngle: CGFloat(M_PI_2),
            clockwise: true
        )
        bezierPath.addArc(
            withCenter: center2,
            radius: halfArcHeight,
            startAngle: CGFloat(M_PI)/2.0,
            endAngle: 3.0*CGFloat(M_PI_2),
            clockwise: true
        )
        bezierPath.addArc(
            withCenter: self.wormView.center,
            radius: innerRadius,
            startAngle: 0,
            endAngle: -CGFloat(M_PI)/2.0,
            clockwise: false
        )
        
        let shapeLayer:CAShapeLayer = CAShapeLayer()
        shapeLayer.path = bezierPath.cgPath
        shapeLayer.fillColor = self.currentBackgroundColor?.cgColor
        
        var colors:[CGColor]?
        if let backgroundColor = self.currentBackgroundColor, let tintColor = self.currentTintColor {
            colors = [
                backgroundColor.cgColor,
                tintColor.cgColor
            ]
        }
        let locations = [0.0, 1.0]
        
        let gradientLayer:CAGradientLayer = CAGradientLayer()
        gradientLayer.frame = view.bounds
        gradientLayer.colors = colors
        gradientLayer.locations = locations as [NSNumber]?
        gradientLayer.mask = shapeLayer
        
        self.wormView.layer.addSublayer(gradientLayer)
    }
    
    private func getTextForState(state: SLThinkerViewControllerLabelTextState) -> String? {
        if let text = self.texts[state] {
            return text
        }
        
        return nil
    }
    
    @objc private func viewTapped() {
        if self.hasBeenTapped {
            return
        }
        
        self.hasBeenTapped = true
        self.delegate?.thinkerViewTapped(tvc: self)
    }
    
    private func setLabelTextFor(
        topState: SLThinkerViewControllerLabelTextState,
        bottomState: SLThinkerViewControllerLabelTextState
        )
    {
        self.topLabel.text = self.getTextForState(state: topState)
        self.bottomLabel.text = self.getTextForState(state: bottomState)
    }
    
    // Mark: Public class functions
    func setState(state: SLThinkerViewControllerState) {
        switch state {
        case .clockwiseStill:
            self.hasBeenTapped = false
            self.wormView.isHidden = true
            self.shouldContinueAnimation = false
            self.currentBackgroundColor = self.secondBackgroundColor
            self.currentTintColor = self.secondBackgroundColor
            self.setLabelTextFor(topState: .clockwiseTopStill, bottomState: .clockwiseBottomStill)
        case .clockwiseMoving:
            self.wormView.isHidden = false
            self.shouldContinueAnimation = true
            self.currentBackgroundColor = self.firstBackgroundColor
            self.currentTintColor = self.secondBackgroundColor
            self.setLabelTextFor(topState: .clockwiseTopMoving, bottomState: .clockwiseBottomMoving)
            self.rotateWormView(shouldRotateClockwise: true)
        case .counterClockwiseStill:
            self.hasBeenTapped = false
            self.wormView.isHidden = true
            self.shouldContinueAnimation = false
            self.currentBackgroundColor = self.firstBackgroundColor
            self.currentTintColor = self.firstBackgroundColor
            self.setLabelTextFor(topState: .counterClockwiseTopStill, bottomState: .counterClockwiseBottomStill)
        case .counterClockwiseMoving:
            self.wormView.isHidden = false
            self.shouldContinueAnimation = true
            self.currentBackgroundColor = self.secondBackgroundColor
            self.currentTintColor = self.firstBackgroundColor
            self.setLabelTextFor(topState: .counterClockwiseTopMoving, bottomState: .counterClockwiseBottomMoving)
            self.rotateWormView(shouldRotateClockwise: false)
        case .inactive:
            self.hasBeenTapped = false
            self.wormView.isHidden = true
            self.shouldContinueAnimation = false
            self.currentBackgroundColor = self.inActiveBackgroundColor
            self.currentTintColor = self.inActiveBackgroundColor
            self.setLabelTextFor(topState: .inactiveTop, bottomState: .inactiveBottom)
        case .connecting:
            self.wormView.isHidden = false
            self.shouldContinueAnimation = true
            self.currentBackgroundColor = self.secondBackgroundColor
            self.currentTintColor = self.firstBackgroundColor
            self.setLabelTextFor(topState: .connectingTop, bottomState: .connectingBottom)
            self.rotateWormView(shouldRotateClockwise: false)
        }
        
        self.thinkerState = state
        self.backgroundView.backgroundColor = self.currentBackgroundColor
        self.updateWormLayer()
    }
    
    func updateTextForState(state: SLThinkerViewControllerState, topText: String?, bottomText: String?) {
        let topTextState:SLThinkerViewControllerLabelTextState
        let bottomTextState:SLThinkerViewControllerLabelTextState
        
        switch state {
        case .inactive:
            topTextState = .inactiveTop
            bottomTextState = .inactiveBottom
        case .clockwiseMoving:
            topTextState = .clockwiseTopMoving
            bottomTextState = .clockwiseBottomMoving
        case .clockwiseStill:
            topTextState = .clockwiseTopStill
            bottomTextState = .clockwiseBottomStill
        case .counterClockwiseMoving:
            topTextState = .counterClockwiseTopMoving
            bottomTextState = .counterClockwiseBottomMoving
        case .counterClockwiseStill:
            topTextState = .counterClockwiseTopStill
            bottomTextState = .counterClockwiseBottomStill
        case .connecting:
            topTextState = .connectingTop
            bottomTextState = .connectingBottom
        }
        
        if let top = topText {
            self.texts[topTextState] = top
        }
        
        if let bottom = bottomText {
            self.texts[bottomTextState] = bottom
        }
        
        if state == self.thinkerState {
            self.setLabelTextFor(topState: topTextState, bottomState: bottomTextState)
        }
    }
}
