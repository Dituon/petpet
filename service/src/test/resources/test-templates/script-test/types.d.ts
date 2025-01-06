declare function register(
    callback: (info: PetpetRuntimeInfo) => Metadata | null | undefined
): string | null

declare function generate(template: PetpetTemplate, request?: RequestData | null): ResultImage

/**
 * 监听全部模板注册完成事件, 新增模板会重复触发此事件
 */
declare function on<T extends "bot_load">(
    eventName: T,
    callback: (event: PetpetBotLoadEvent) => void
)

/**
 * 监听 bot 环境中本模板触发事件
 */
declare function on<T extends "bot_send">(
    eventName: T,
    callback: (event: BotSendEvent) => void
)

// TODO
/**
 * 监听 bot 环境中其它模板触发事件
 */
declare function on<T extends "bot_presend">(
    eventName: T,
    callback: (event: BotPreSendEvent) => void
)

/**
 * 监听全部模板注册完成事件, 新增模板会重复触发此事件
 */
declare function on<T extends "load">(
    eventName: T,
    callback: (event: PetpetLoadEvent) => void
)

/**
 * 监听通用本模板触发事件, 在 bot 环境下不会触发
 */
declare function on<T extends "send">(
    eventName:  T,
    callback: (event: PetpetSendEvent) => void
)

declare function on(
    eventName: string,
    callback: (event: object) => void
): void

declare function isFileExists(path: string): boolean

declare const log: Logger

interface Logger {
    info(message: any): void
    warn(message: any): void
    error(message: any): void
    debug(message: any): void
}

interface PetpetBotLoadEvent extends PetpetLoadEvent {
    defaultTemplate: string
}

interface PetpetLoadEvent {
    templates: PetpetTemplateInfo[]
}

interface PetpetSendEvent {
    request: RequestData

    generate(template: PetpetTemplate, request?: RequestData | null): ResultImage
    result(res: PetpetTemplate | ResultImage): void
}

interface BotSendEvent {
    request: BotSendRequestData

    responseInForward(bool: boolean): void
    responseNewParagraph(): void
    response(msg: string): void
    responseImage(
        image: (string | ResultImage | PetpetTemplate),
        cacheable?: boolean
    ): void
    generate(template: PetpetTemplate, request?: RequestData | null): ResultImage
}

interface BotPreSendEvent {
    request: BotSendRequestData
    cancel(): void
}

interface RequestData {
    text: Record<string, string>
    image: Record<string, string>
}

interface BotSendRequestData extends RequestData {
    text: {
        from: string
        to: string
        group: string
        bot: string
        from_id: string
        to_id: string
        group_id: string
        bot_id: string
        [key: string]: string
    }
    image: {
        from: string
        to: string
        group: string
        bot: string
        [key: string]: string
    }
}

interface PetpetTemplateInfo {
    id: string
    type: "template" | "script"
    preview: string
    metadata: Metadata
}

interface PetpetRuntimeInfo {
    version: number
    scriptApiVersion: number
    platform: 'jvm' | 'js' | 'native'
    jsEngine: 'nashorn' | 'graaljs' | 'browser' | 'quickjs'
    drawingApi: 'awt' | 'canvas' | 'skia'
    features: ('http-server' | 'qq-bot')[]
}

interface ResultImage {
    width: number
    height: number
    length: number
    mime: string
    suffix: string

    save(path: string): boolean
}

// template types

interface Metadata {
    alias?: string[]
    desc?: string
    author?: string
    hidden?: boolean
    tags?: string[]
    inRandomList?: boolean
    [key: string]: any
}

interface Canvas {
    width: Length
    height: Length
    length?: number | string
    color?: Color
    reverse?: boolean
}

type TemplateElement =
    | ImageElement
    | TextElement
    | BackgroundElement

interface BackgroundElement {
    type: 'background'
    src: string
}

interface PetpetTemplate {
    type: 'gif' | 'image'
    version?: number
    metadata?: Metadata
    elements: TemplateElement[]
    canvas?: Canvas
    delay?: number | number[]
    fps?: number | number[]
}


// image types

interface ImageElement {
    type: "image"
    key: string | string[]
    default?: string | string[]
    src?: string | string[]
    coords: ImageCoords | ImageCoords[]
    crop?: Crop | Crop[]
    fit?: Fit | Fit[]
    position?: Offset | Offset[]
    angle?: number | number[]
    origin?: Offset | Offset[]
    opacity?: number | number[]
    border_radius?: BorderRadius | BorderRadius[]
    filter?: ImageFilter[]
}

type ImageCoords = XYWHCoords | DeformCoords;

type XYWHCoords = [
    x: Length, // x (left)
    y: Length, // y (top)
    width: Length, // width
    height: Length // height
];

type DeformCoords = [
    [x1: Length, y1: Length], // 左上角
    [x2: Length, y2: Length], // 左下角
    [x3: Length, y3: Length], // 右下角
    [x4: Length, y4: Length], // 右上角
    [anchorX: Length, anchorY: Length] // 锚点
];

type Crop = [
    x1: Length, y1: Length, x2?: Length, y2?: Length
]

type Fit = "fill" | "contain" | "cover"

type Offset = OffsetKeyword | Length | `${XOffsetKeyword | Length} ${YOffsetKeyword | Length}`

type BorderRadius = Length | `${Length} ${Length}`
    | `${Length} ${Length} ${Length}` | `${Length} ${Length} ${Length} ${Length}`

type Length = number | `${number}` | `${number}px` | `${number}%`
    | `${number}vw` | `${number}vh` | `${number}cw` | `${number}ch`
    | string // TODO: dynamic length expr

type XOffsetKeyword = "left" | "right" | "center"
type YOffsetKeyword = "top" | "bottom" | "center"
type OffsetKeyword = XOffsetKeyword | YOffsetKeyword

// image filter types

interface BaseFilter {
    type: string;
}

interface SwirlFilter extends BaseFilter {
    type: 'swirl';
    radius?: number | number[];
    angle?: number | number[];
    x?: number | number[];
    y?: number | number[];
}

interface BulgeFilter extends BaseFilter {
    type: 'bulge';
    radius?: number | number[];
    strength?: number | number[];
    x?: number | number[];
    y?: number | number[];
}

interface SwimFilter extends BaseFilter {
    type: 'swim';
    scale?: number | number[];
    stretch?: number | number[];
    angle?: number | number[];
    amount?: number | number[];
    turbulence?: number | number[];
    time?: number | number[];
}

interface BlurFilter extends BaseFilter {
    type: 'blur';
    radius?: number | number[];
}

interface ContrastFilter extends BaseFilter {
    type: 'contrast';
    brightness?: number | number[];
    contrast?: number | number[];
}

interface HSBFilter extends BaseFilter {
    type: 'hsb';
    hue?: number | number[];
    saturation?: number | number[];
    brightness?: number | number[];
}

interface HalftoneFilter extends BaseFilter {
    type: 'halftone';
    angle?: number | number[];
    radius?: number | number[];
    x?: number | number[];
    y?: number | number[];
}

interface DotScreenFilter extends BaseFilter {
    type: 'dot_screen';
    angle?: number | number[];
    radius?: number | number[];
    x?: number | number[];
    y?: number | number[];
}

interface NoiseFilter extends BaseFilter {
    type: 'noise';
    amount?: number | number[];
}

interface DenoiseFilter extends BaseFilter {
    type: 'denoise';
    exponent?: number | number[];
}

interface OilFilter extends BaseFilter {
    type: 'oil';
    skip?: number | number[];
    range?: number | number[];
    levels?: number | number[];
}

interface MirageFilter extends BaseFilter {
    type: 'mirage';
    key?: string | string[];
    default?: string | string[];
    inner_scale?: number | number[];
    cover_scale?: number | number[];
    inner_desat?: number | number[];
    cover_desat?: number | number[];
    weight?: number | number[];
    max_size?: number | number[];
    colored?: boolean | boolean[];
}

interface GrayFilter extends BaseFilter {
    type: 'gray';
}

interface BinarizeFilter extends BaseFilter {
    type: 'binarize';
}

interface MirrorFilter extends BaseFilter {
    type: 'mirror';
}

interface FlipFilter extends BaseFilter {
    type: 'flip';
}

type ImageFilter =
    | SwirlFilter
    | BulgeFilter
    | SwimFilter
    | BlurFilter
    | ContrastFilter
    | HSBFilter
    | HalftoneFilter
    | DotScreenFilter
    | NoiseFilter
    | DenoiseFilter
    | OilFilter
    | MirageFilter
    | GrayFilter
    | BinarizeFilter
    | MirrorFilter
    | FlipFilter;

// text types

interface TextElement {
    type: 'text'
    text: string | string[]
    coords: TextCoords | TextCoords[]
    color?: Color | Color[]
    angle?: number | number[]
    origin?: Offset | Offset[]
    font?: string | string[]
    size?: Length | Length[]
    align?: TextAlign | TextAlign[]
    baseline?: TextBaseline | TextBaseline[]
    wrap?: TextWrap | TextWrap[]
    style?: TextStyle | TextStyle[]
    stroke_color?: Color | Color[]
    stroke_size?: Length | Length[]
}

type Color = `#${string}`

interface TextCoords {
    x: Length
    y: Length
    maxWidth?: Length
}

type TextAlign = 'left' | 'center' | 'right';

type TextBaseline = 'top' | 'middle' | 'bottom';

type TextWrap = 'none' | 'break' | 'zoom';

type TextStyle = 'plain' | 'bold' | 'italic' | 'bold_italic';
