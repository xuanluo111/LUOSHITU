// 洛视频 release 冒烟专用 JS spider（QuickJS 模块格式）。
// 用于在 R8 混淆构建下验证：配置加载 -> quickjs 引擎初始化 ->
// home/homeVod/category/detail/search/play 全链路。
// 用法见同目录 smoke.sh。
const VODS = [
    {
        vod_id: 'v1',
        vod_name: '冒烟测试影片一',
        vod_pic: '',
        type_name: '冒烟分类',
        vod_year: '2026',
        vod_area: '大陆',
        vod_remarks: '1080p',
        vod_actor: '冒烟测试员',
        vod_content: '用于验证 release 构建下 QuickJS 爬虫链路的测试影片。'
    },
    {
        vod_id: 'v2',
        vod_name: '冒烟测试影片二',
        vod_pic: '',
        type_name: '冒烟分类',
        vod_year: '2026',
        vod_area: '大陆',
        vod_remarks: '4K',
        vod_actor: '冒烟测试员',
        vod_content: '用于验证 release 构建下 QuickJS 爬虫链路的测试影片。'
    }
];

const PLAY_URL = 'https://media.w3.org/2010/05/sintel/trailer.mp4';

export default {
    init(cfg) {
        return '';
    },
    home(filter) {
        return JSON.stringify({class: [{type_id: 't1', type_name: '冒烟分类'}], filters: {}});
    },
    homeVod() {
        return JSON.stringify({list: VODS});
    },
    category(tid, pg, filter) {
        return JSON.stringify({list: VODS});
    },
    detail(id) {
        const vod = {...VODS[0], vod_play_from: '测试直链', vod_play_url: '第1集$' + PLAY_URL};
        return JSON.stringify({list: [vod]});
    },
    search(wd, quick) {
        return JSON.stringify({list: VODS});
    },
    play(flag, id, flags) {
        return JSON.stringify({parse: 0, url: id, header: {}});
    }
};
